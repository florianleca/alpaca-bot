package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class Strategy1Service {

    private final Logger logger = LoggerFactory.getLogger(Strategy1Service.class);
    private final OrderService orderService;
    private final AssetService assetService;
    private final BarService barService;
    private final LatestQuoteService latestQuoteService;
    private final Strategy1TicketRepository strategy1TicketRepository;
    private final PositionService positionService;

    @Value("${NOTIONAL}")
    private double notional;
    @Value("${THRESHOLD}")
    private double threshold;
    @Value("${TIMEFRAME}")
    private String barTimeFrameLabel;
    @Value("${PERIOD_LENGTH}")
    private int periodLength;
    @Value("${PERIOD_LENGTH_UNIT}")
    private String periodLengthUnitLabel;
    @Value("${PREVIOUSLY_BOUGHT_PERCENTAGE}")
    private double previouslyBoughtPercentage;
    @Value("${GAIN_PERCENTAGE}")
    private double gainPercentage;

    @Autowired
    public Strategy1Service(OrderService orderService,
                            AssetService assetService,
                            BarService barService,
                            LatestQuoteService latestQuoteService,
                            Strategy1TicketRepository strategy1TicketRepository,
                            PositionService positionService) {
        this.orderService = orderService;
        this.assetService = assetService;
        this.barService = barService;
        this.latestQuoteService = latestQuoteService;
        this.strategy1TicketRepository = strategy1TicketRepository;
        this.positionService = positionService;
    }

    @Scheduled(cron = "${STRATEGY_1_CRON}")
    public void checkBuyOpportunities() throws IOException {
        logger.info("[STRATEGY 1] [BUY OPPORTUNITIES]");
        List<AssetModel> assets = assetService.getAssetsList(); // On commence par prendre la liste de tous les assets existants
        assets = removeAssetsUnderThreshold(assets);            // On retire les assets dont le prix ne représente pas une opportunité
        assets = removeAssetsAlreadyBought(assets);             // On retire les assets ayant un ordre d'achat filled mais pas encore revendu (pas de dual sellOrder) dont le prix est trop proche du cours actuel
        assets.forEach(this::orderAssetAndCreateTicket);
    }

    private void orderAssetAndCreateTicket(AssetModel asset) {
        double positionQtyBeforeBuyOrder = positionService.getCurrentQtyOfAsset(asset.getSymbol());
        logger.info("Trying to buy some {}...", asset.getSymbol());
        OrderModel buyOrder = orderService.createMarketNotionalOrder(
                asset.getSymbol(),
                String.valueOf(notional),
                OrderSide.BUY,
                TimeInForce.IOC);
        createTicket(buyOrder, positionQtyBeforeBuyOrder);
    }

    public void createTicket(OrderModel buyOrder, double positionQtyBeforeBuyOrder) {
        Strategy1TicketModel strategy1TicketModel = new Strategy1TicketModel(buyOrder, positionQtyBeforeBuyOrder);
        strategy1TicketRepository.save(strategy1TicketModel);
        logger.info("Created and archived new ticket: {}", strategy1TicketModel);
        updateTicket(strategy1TicketModel);
    }

    public void updateTicket(Strategy1TicketModel ticket) {
        switch (ticket.getStatus()) {
            case BUY_UNFILLED -> updateTicket1to2(ticket);
            case BUY_FILLED_SELL_UNFILLED -> updateTicket2to3(ticket);
            case COMPLETE -> logger.warn("Nothing to update, ticket already complete");
        }
    }

    private void updateTicket1to2(Strategy1TicketModel ticket) {
        logger.info("Trying to update a {} ticket from 'BUY_UNFILLED' to 'BUY_FILLED_SELL_UNFILLED'", ticket.getSymbol());
        OrderModel potentiallyFilledBuyOrder = orderService.getOrderById(ticket.getBuyOrderId());
        if (potentiallyFilledBuyOrder.getStatus().equals("filled")) {
            logger.info("[\uD83D\uDCB8] Bought ${} worth of {}!", notional, ticket.getSymbol().split("/")[0]);
            ticket.setPositionQtyAfterBuyOrder(positionService.getCurrentQtyOfAsset(ticket.getSymbol()));
            OrderModel buyOrder = orderService.getOrderById(ticket.getBuyOrderId());
            OrderModel sellOrder = orderService.createLimitQuantityOrder(
                    ticket.getSymbol(),
                    String.valueOf(ticket.getPositionQtyAfterBuyOrder() - ticket.getPositionQtyBeforeBuyOrder()),
                    OrderSide.SELL,
                    TimeInForce.GTC,
                    String.valueOf(buyOrder.getFilledAvgPrice() * (1 + (gainPercentage / 100))));
            ticket.setSellOrderId(sellOrder.getId());
            ticket.setStatus(Strategy1TicketStatus.BUY_FILLED_SELL_UNFILLED);
            ticket.setAverageFilledBuyPrice(buyOrder.getFilledAvgPrice());
            strategy1TicketRepository.save(ticket);
            logger.info("Successfully updated {} ticket from 'BUY_UNFILLED' to 'BUY_FILLED_SELL_UNFILLED'", ticket.getSymbol());
        } else {
            logger.info("Buy order of {} has not been filled - Deleting ticket from database", ticket.getSymbol());
            strategy1TicketRepository.delete(ticket);
        }

    }

    private void updateTicket2to3(Strategy1TicketModel ticket) {
        logger.info("Trying to update a {} ticket from 'BUY_FILLED_SELL_UNFILLED' to 'COMPLETE'", ticket.getSymbol());
        OrderModel potentiallyFilledSellOrder = orderService.getOrderById(ticket.getSellOrderId());
        if (potentiallyFilledSellOrder.getStatus() == null) {
            logger.error("Sell order of id {} has null status", ticket.getSellOrderId());
        } else if (potentiallyFilledSellOrder.getStatus().equals("filled")) {
            ticket.setStatus(Strategy1TicketStatus.COMPLETE);
            strategy1TicketRepository.save(ticket);
            logger.info("This {} ticket is now completed!", ticket.getSymbol());
        } else {
            logger.warn("Couldn't update this {} ticket, because the sell order status isn't 'filled' but '{}'", ticket.getSymbol(), potentiallyFilledSellOrder.getStatus());
        }
    }

    private List<AssetModel> removeAssetsUnderThreshold(List<AssetModel> assets) {
        List<AssetModel> filteredAssets = new ArrayList<>();
        assets.forEach(asset -> {
            logger.info("Is {}'s price interesting?", asset.getName().split(" /")[0].trim());
            try {
                asset.setLatestValue(latestQuoteService.getLatestQuote(asset));
                double maxHigh = barService.getMaxHighOnPeriod(asset, barTimeFrameLabel, periodLength, periodLengthUnitLabel);
                maxHigh = Math.max(maxHigh, asset.getLatestValue());
                if (decreasedMoreThanThreshold(asset, maxHigh)) {
                    filteredAssets.add(asset);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        logger.info("Number of opportunities: {}/{}", filteredAssets.size(), assets.size());
        return filteredAssets;
    }

    public boolean decreasedMoreThanThreshold(AssetModel asset, double maxHigh) {
        if (asset.getLatestValue() > maxHigh) {
            throw new RuntimeException("Asset latest value should be above its maxHigh");
        }
        double decreasePercent = ((maxHigh - asset.getLatestValue()) / maxHigh) * 100;
        logAssetThresholdState(asset, decreasePercent, maxHigh);
        return decreasePercent >= threshold;
    }

    private List<AssetModel> removeAssetsAlreadyBought(List<AssetModel> assets) {
        List<AssetModel> filteredAssets = new ArrayList<>();
        assets.forEach(asset -> {
            logger.info("Do we already have uncompleted {} tickets with similar buying price?", asset.getName().split(" /")[0].trim());
            checkAssetUncompletedTickets(asset, filteredAssets);
        });
        logger.info("Number of opportunities: {}/{}", filteredAssets.size(), assets.size());
        return filteredAssets;
    }

    private void checkAssetUncompletedTickets(AssetModel asset, List<AssetModel> filteredAssets) {
        // pour chaque asset, récupérer la liste des uncompleted tickets de cet asset
        List<Strategy1TicketModel> tickets = strategy1TicketRepository.findUncompletedTickets(asset.getSymbol());
        // si la liste est vide : is ok
        if (tickets.isEmpty()) {
            logger.info("\uD83D\uDFE2 No uncompleted {} tickets in database", asset.getSymbol());
            filteredAssets.add(asset);
        } else {
            // sinon : on calcule la valeur minimale
            Double minBuyPrice = minBuyPriceFromTicketList(tickets);
            // si la valeur minimum d'achat est plus grande que (valeur actuelle + delta) :  is ok
            if (minBuyPrice > (1 + (previouslyBoughtPercentage / 100)) * asset.getLatestValue()) {
                logger.info("\uD83D\uDFE2 Min value of {} tickets in DB is high enough ({}) compared to current value ({})", asset.getSymbol(), minBuyPrice, asset.getLatestValue());
                filteredAssets.add(asset);
            } else {
                logger.info("\uD83D\uDD34 Min value of {} tickets in DB is NOT high enough ({}) compared to current value ({})", asset.getSymbol(), minBuyPrice, asset.getLatestValue());
            }
        }
    }

    public Double minBuyPriceFromTicketList(List<Strategy1TicketModel> tickets) {
        return tickets.stream()
                .map(Strategy1TicketModel::getAverageFilledBuyPrice)
                .min(Double::compare)
                .orElse(-1.);
    }

    private void logAssetThresholdState(AssetModel asset, double decreasePercent, double maxHigh) {
        String decreasedPercentString = BigDecimal.valueOf(decreasePercent)
                .setScale(2, RoundingMode.HALF_DOWN)
                .toString();
        String state;
        if (decreasePercent >= threshold) {
            state = "\uD83D\uDFE2";
        } else {
            state = "\uD83D\uDD34";
        }
        logger.info("[{}\uD83D\uDCC9 {}%] {} ({}): [Highest: ${}] [Latest: {}$]",
                state,
                decreasedPercentString,
                asset.getName().split(" /")[0].trim(),
                asset.getSymbol(),
                maxHigh,
                asset.getLatestValue());
    }

    public Strategy1TicketModel orderToTicket(OrderModel order) {
        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
        if (ticket != null) {
            return ticket;
        } else {
            throw new RuntimeException("Requested order of id " + order.getId() + " was not linked to any ticket in database");
        }
    }

    @Scheduled(cron = "${STRATEGY_1_UPDATE_CRON}")
    public void updateUncompletedTickets() {
        logger.info("Updating tickets...");
        List<Strategy1TicketModel> uncompletedTickets = strategy1TicketRepository.findUncompletedTickets();
        uncompletedTickets.forEach(this::updateTicket);
        logger.info("Update done!");
    }

}
