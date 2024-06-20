package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Setter
public class Strategy1Service {

    private final OrderService orderService;
    private final Strategy1TicketRepository strategy1TicketRepository;
    private final Strategy1OpportunityChecker strategy1OpportunityChecker;
    private final PositionService positionService;
    private Logger logger = LoggerFactory.getLogger(Strategy1Service.class);

    @Value("${NOTIONAL}")
    private double notional;

    @Value("${GAIN_PERCENTAGE}")
    private double gainPercentage;

    @Autowired
    public Strategy1Service(OrderService orderService,
                            Strategy1TicketRepository strategy1TicketRepository,
                            Strategy1OpportunityChecker strategy1OpportunityChecker,
                            PositionService positionService) {
        this.orderService = orderService;
        this.strategy1TicketRepository = strategy1TicketRepository;
        this.strategy1OpportunityChecker = strategy1OpportunityChecker;
        this.positionService = positionService;
    }

    @Scheduled(cron = "${STRATEGY_1_CRON}")
    public void checkBuyOpportunitiesAndBuy() throws IOException {
        updateUncompletedTickets();
        List<AssetModel> assets = strategy1OpportunityChecker.checkBuyOpportunities();
        assets.forEach(this::createBuyOrder);
    }

    public void createBuyOrder(AssetModel asset) {
        logger.info("Trying to buy some {}...", asset.getSymbol());
        try {
            OrderModel buyOrder = orderService.createMarketNotionalOrder(
                    asset.getSymbol(),
                    String.valueOf(notional),
                    OrderSide.BUY,
                    TimeInForce.GTC);
            if (buyOrder == null) {
                logger.error("Failed to buy some {}", asset.getSymbol());
                return;
            }
            Strategy1TicketModel ticket = new Strategy1TicketModel();
            ticket.setSymbol(asset.getSymbol());
            ticket.setBuyOrderId(buyOrder.getId());
            ticket.setStatus(Strategy1TicketStatus.BUY_UNFILLED);
            strategy1TicketRepository.save(ticket);
        } catch (IOException e) {
            logger.error("Failed to buy some {}: {}", asset.getSymbol(), e.getMessage());
        }
    }

    public void createSellOrder(Strategy1TicketModel ticket) {
        try {
            OrderModel sellOrder = orderService.createLimitQuantityOrder(
                    ticket.getSymbol(),
                    String.valueOf(ticket.getBoughtQuantityAfterFees()),
                    OrderSide.SELL,
                    TimeInForce.GTC,
                    String.valueOf(ticket.getAverageFilledBuyPrice() * (1 + (gainPercentage / 100))));
            if (sellOrder == null) {
                logger.error("Failed to create a sell order for {} ticket", ticket.getSymbol());
                return;
            }
            ticket.setSellOrderId(sellOrder.getId());
            ticket.setStatus(Strategy1TicketStatus.SELL_UNFILLED);
            strategy1TicketRepository.save(ticket);
            logger.info("Successfully created a SELL order for {} ticket", ticket.getSymbol());
        } catch (IOException e) {
            logger.error("Failed to create a sell order for {}: {}", ticket.getSymbol(), e.getMessage());
        }
    }

    //@Scheduled(cron = "${STRATEGY_1_UPDATE_CRON}")
    public void updateUncompletedTickets() {
        logger.info("Updating tickets...");
        List<Strategy1TicketModel> uncompletedTickets = strategy1TicketRepository.findUncompletedTickets();
        uncompletedTickets.forEach(this::updateUncompletedTicket);
        logger.info("Update done!");
    }

    public void updateUncompletedTicket(Strategy1TicketModel strategy1TicketModel) {
        switch (strategy1TicketModel.getStatus()) {
            case BUY_UNFILLED -> updateBuyUnfilledTicket(strategy1TicketModel);
            case SELL_UNFILLED -> updateSellUnfilledTicket(strategy1TicketModel);
        }
    }

    public void updateBuyUnfilledTicket(Strategy1TicketModel strategy1TicketModel) {
        try {
            OrderModel orderModel = orderService.getOrderById(strategy1TicketModel.getBuyOrderId());
            if (orderModel.getStatus().equals("filled")) {
                processFilledBuyOrder(orderModel);
            }
        } catch (IOException e) {
            logger.error("Failed to retrieve buy order of ticket {}: {}", strategy1TicketModel.getId(), e.getMessage());
        }
    }

    public void updateSellUnfilledTicket(Strategy1TicketModel strategy1TicketModel) {
        try {
            OrderModel orderModel = orderService.getOrderById(strategy1TicketModel.getSellOrderId());
            if (orderModel.getStatus().equals("filled")) {
                processFilledSellOrder(orderModel);
            }
        } catch (IOException e) {
            logger.error("Failed to retrieve sell order of ticket {}: {}", strategy1TicketModel.getId(), e.getMessage());
        }
    }

    /**
     * If a buy order is filled, we create a sell order
     *
     * @param order the buy order that was filled
     */
    public void processFilledBuyOrder(OrderModel order) {
        logger.info("[💸] Bought ${} worth of {}!", notional, order.getSymbol().split("/")[0]);
        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
        if (ticket == null) {
            logger.error("Trying to process a filled buy order that was not linked to any ticket in the database: {}", order.getId());
            return;
        }
        if (ticket.getStatus() != Strategy1TicketStatus.BUY_UNFILLED) {
            logger.error("Trying to process a filled buy order which ticket was not \"BUY_UNFILLED\" but {}", ticket.getStatus());
            return;
        }

        try {
            ticket.setBoughtQuantityAfterFees(Double.parseDouble(positionService.getAnOpenPosition(ticket.getSymbol()).getQuantityAvailable()));
            ticket.setBoughtQuantity(order.getFilledQuantity());
            ticket.setAverageFilledBuyPrice(order.getFilledAvgPrice());
            createSellOrder(ticket);
        } catch (IOException e) {
            logger.error("Failed to retrieve the available quantity of asset {}: {}", ticket.getSymbol(), e.getMessage());
        }
    }

    public void processFilledSellOrder(OrderModel order) {
        logger.info("Processing a filled SELL {} order", order.getSymbol());
        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
        if (ticket == null) {
            logger.error("Trying to process a filled sell order that was not linked to any ticket in the database: {}", order.getId());
            return;
        }
        if (ticket.getStatus() != Strategy1TicketStatus.SELL_UNFILLED) {
            logger.error("Trying to process a filled sell order which ticket was not \"SELL_UNFILLED\" but {}", ticket.getStatus());
            return;
        }
        ticket.setStatus(Strategy1TicketStatus.COMPLETE);
        ticket.setSoldQuantity(order.getFilledQuantity());
        ticket.setAverageFilledSellPrice(order.getFilledAvgPrice());
        strategy1TicketRepository.save(ticket);
        logger.info("This {} ticket is now completed!", ticket.getSymbol());
    }

}
