package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionModel;
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

    @Autowired
    private OrderService orderService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private BarService barService;

    @Autowired
    private LatestQuoteService latestQuoteService;

    @Autowired
    private Strategy1TicketRepository strategy1TicketRepository;

    @Autowired
    private PositionService positionService;

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

    @Value("${WEEKS_BACK_FOR_GLOBAL_MAX}")
    private int weeksBackForGlobalMax;

    @Value("${GLOBAL_MAX_PERCENTAGE}")
    private double globalMaxPercentage;

    @Value("${PREVIOUSLY_BOUGHT_PERCENTAGE}")
    private double previouslyBoughtPercentage;

    @Value("${GAIN_PERCENTAGE}")
    private double gainPercentage;


    private final Logger logger = LoggerFactory.getLogger(Strategy1Service.class);

    // @Scheduled(cron = "0 */1 * * * *")
    public void checkBuyOpportunities() throws IOException {
        logger.info("[STRATEGY 1] [BUY OPPORTUNITIES]");
        List<AssetModel> assets = assetService.getAssetsList(); // On commence par prendre la liste de tous les assets existants
        assets = removeAssetsUnderThreshold(assets);            // On retire les assets dont le prix ne représente pas une opportunité
        assets = removeUnfilledBuyOrders(assets);               // On retire les assets ayant un ordre d'achat unfilled
        // assets = removeAssetsAlreadyBought(assets);             // On retire les assets ayant un ordre d'achat filled mais pas encore revendu (pas de dual sellOrder) dont le prix est trop proche du cours actuel
        assets.forEach(asset -> {
            try {
                double positionQtyBeforeBuyOrder = positionService.getCurrentQtyOfAsset(asset.getSymbol());
                OrderModel buyOrder = orderService.createLimitNotionalOrder(
                        asset.getSymbol(),
                        String.valueOf(notional),
                        OrderSide.BUY,
                        TimeInForce.IOC,
                        String.valueOf(asset.getLatestValue()));
                buyOrder = orderService.getOrderById(buyOrder.getId());         // sinon même en IOC le statut aurait été 'pending-new'
                orderService.archive(buyOrder);
                switch (buyOrder.getStatus()) {
                    case "filled" -> {
                        createTicket(buyOrder, positionQtyBeforeBuyOrder);
                        logBoughtAsset(asset);
                    }
                    case "canceled" -> logger.warn("IOC order for buying {} has been canceled", buyOrder.getSymbol());
                    default -> throw new RuntimeException("IOC order is neither filled or canceled");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createTicket(OrderModel buyOrder, double positionQtyBeforeBuyOrder) throws IOException {
        Strategy1TicketModel strategy1TicketModel = new Strategy1TicketModel(buyOrder, positionQtyBeforeBuyOrder);
        strategy1TicketRepository.save(strategy1TicketModel);
        logger.info("Created and archived new ticket: {}", strategy1TicketModel);
        updateTicket(strategy1TicketModel);
    }

    private void updateTicket(Strategy1TicketModel ticket) throws IOException {
        switch (ticket.getStatus()) {
            case BUY_UNFILLED -> updateTicket1to2(ticket);
            case BUY_FILLED_SELL_UNFILLED -> updateTicket2to3(ticket);
            case COMPLETE -> logger.warn("Nothing to update, ticket already complete");
        }
    }

    private void updateTicket1to2(Strategy1TicketModel ticket) throws IOException {
        logger.info("Trying to update a {} ticket from 'BUY_UNFILLED' to 'BUY_FILLED_SELL_UNFILLED'", ticket.getSymbol());
        OrderModel potentiallyFilledBuyOrder = orderService.getOrderById(ticket.getBuyOrderId());
        if (potentiallyFilledBuyOrder.getStatus().equals("filled")) {
            ticket.setPositionQtyAfterBuyOrder(positionService.getCurrentQtyOfAsset(ticket.getSymbol()));
            OrderModel buyOrder = orderService.getOrderById(ticket.getBuyOrderId());
            OrderModel sellOrder = orderService.createLimitQuantityOrder(
                    ticket.getSymbol(),
                    String.valueOf(ticket.getPositionQtyAfterBuyOrder() - ticket.getPositionQtyBeforeBuyOrder()),
                    OrderSide.SELL,
                    TimeInForce.GTC,
                    String.valueOf(buyOrder.getFilledAvgPrice() * (1 + (gainPercentage / 100 ))));
            ticket.setSellOrderId(sellOrder.getId());
            ticket.setStatus(Strategy1TicketStatus.BUY_FILLED_SELL_UNFILLED);
        } else {
            logger.warn("Couldn't update this {} ticket, because the buy order status isn't 'filled' but '{}'", ticket.getSymbol(), potentiallyFilledBuyOrder.getStatus());
        }
    }

    private void updateTicket2to3(Strategy1TicketModel ticket) throws IOException {
        logger.info("Trying to update a {} ticket from 'BUY_FILLED_SELL_UNFILLED' to 'COMPLETE'", ticket.getSymbol());
        OrderModel potentiallyFilledSellOrder = orderService.getOrderById(ticket.getSellOrderId());
        if (potentiallyFilledSellOrder.getStatus().equals("filled")) {
            ticket.setStatus(Strategy1TicketStatus.COMPLETE);
        } else {
            logger.warn("Couldn't update this {} ticket, because the sell order status isn't 'filled' but '{}'", ticket.getSymbol(), potentiallyFilledSellOrder.getStatus());
        }
    }

    private List<AssetModel> removeAssetsUnderThreshold(List<AssetModel> assets) {
        List<AssetModel> filteredAssets = new ArrayList<>();
        assets.forEach(asset -> {
            logger.info("Is " + asset.getName().split(" /")[0].trim() + "'s price interesting?");
            try {
                asset.setLatestValue(latestQuoteService.getLatestQuote(asset));
                double maxHigh = barService.getMaxHighOnPeriod(asset, barTimeFrameLabel, periodLength, periodLengthUnitLabel);
                maxHigh = Math.max(maxHigh, asset.getLatestValue());
                if (decreasedMoreThanThreshold(asset, maxHigh) && maxHighIsOrdinary(asset, maxHigh)) {
                    filteredAssets.add(asset);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        logger.info("Number of opportunities: " + filteredAssets.size() + "/" + assets.size());
        return filteredAssets;
    }

    private boolean decreasedMoreThanThreshold(AssetModel asset, double maxHigh) throws IOException {
        double decreasePercent = ((maxHigh - asset.getLatestValue()) / maxHigh) * 100;
        logAssetThresholdState(asset, decreasePercent, maxHigh);
        return decreasePercent >= threshold;
    }

    private boolean maxHighIsOrdinary(AssetModel asset, double maxHigh) throws IOException {
        double moreGlobalMax = barService.getMaxHighOnPeriod(asset, BarTimeFrame.DAY1, weeksBackForGlobalMax, PeriodLengthUnit.WEEK);
        boolean maxHighIsOrdinary = maxHigh < (1 - (globalMaxPercentage / 100)) * moreGlobalMax;
        if (maxHighIsOrdinary) logger.info("[\uD83D\uDFE2 Past week max value: $" + moreGlobalMax);
        else logger.info("[\uD83D\uDD34\uD83D\uDD34 Past week max value: $" + moreGlobalMax);
        return maxHighIsOrdinary;
    }

//    private List<AssetModel> removeAssetsAlreadyBought(List<AssetModel> assets) {
//        List<AssetModel> filteredAssets = new ArrayList<>();
//        assets.forEach(asset -> {
//            logger.info("Is " + asset.getName().split(" /")[0].trim() + "already brought too close?");
//            // UnsoldBuyOrder = ordre d'achat dont l'ordre de vente dual a une date filled_at null
//            List<OrderModel> unsoldOrders = orderService.getUnsoldBuyOrders(asset.getSymbol());
//            int nbOfUnsoldOrdersOfAsset = unsoldOrders.size();
//            double minValueInPortfolio = unsoldOrders.stream().mapToDouble(OrderModel::getLimitPrice).min().orElse(Double.MAX_VALUE);
//            String state;
//            if (nbOfUnsoldOrdersOfAsset == 0 || asset.getLatestValue() < (1 - (previouslyBoughtPercentage / 100)) * minValueInPortfolio) {
//                state = "\uD83D\uDFE2";
//                filteredAssets.add(asset);
//            } else state = "\uD83D\uDD34";
//            logger.info("[{}] {} ({}): [Unsold orders in DB: {}] [Min limit_price of them: {}] [Latest value: {}]",
//                    state,
//                    asset.getName().split(" /")[0].trim(),
//                    asset.getSymbol(),
//                    nbOfUnsoldOrdersOfAsset,
//                    minValueInPortfolio,
//                    asset.getLatestValue());
//        });
//        logger.info("Number of opportunities: {}/{}", filteredAssets.size(), assets.size());
//        return filteredAssets;
//    }

    private List<AssetModel> removeUnfilledBuyOrders(List<AssetModel> assets) {
        List<AssetModel> filteredAssets = new ArrayList<>();
        assets.forEach(asset -> {
            if (orderService.countUnfilledBuyOrder(asset.getSymbol()) == 0) {
                filteredAssets.add(asset);
            } else {
                logger.warn("Asset {} was not bought because an unfilled buy order already is in database.", asset.getSymbol());
            }
        });
        return filteredAssets;
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
        logger.info("[" + state + "\uD83D\uDCC9 " + decreasedPercentString + "%] "
                + asset.getName().split(" /")[0].trim()
                + " ("
                + asset.getSymbol()
                + "): [Highest: $" + maxHigh + "] [Latest: $" + asset.getLatestValue() + "]");
    }

    private void logBoughtAsset(AssetModel asset) {
        logger.info("[\uD83D\uDCB8] Bought $" + notional
                + " worth of "
                + asset.getName().split(" /")[0]
                + " ("
                + asset.getSymbol().split("/")[0]
                + ")");
    }

}
