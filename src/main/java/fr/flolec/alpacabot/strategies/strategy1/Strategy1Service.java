package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Strategy1Service {

    private final Logger logger = LoggerFactory.getLogger(Strategy1Service.class);
    private final OrderService orderService;
    private final Strategy1TicketRepository strategy1TicketRepository;

    @Value("${NOTIONAL}")
    private double notional;

    @Value("${GAIN_PERCENTAGE}")
    private double gainPercentage;

    @Autowired
    public Strategy1Service(OrderService orderService,
                            Strategy1TicketRepository strategy1TicketRepository) {
        this.orderService = orderService;
        this.strategy1TicketRepository = strategy1TicketRepository;
    }

    public void createBuyOrder(AssetModel asset) {
        logger.info("Trying to buy some {}...", asset.getSymbol());
        orderService.createMarketNotionalOrder(
                asset.getSymbol(),
                String.valueOf(notional),
                OrderSide.BUY,
                TimeInForce.IOC);
    }

    public void createSellOrder(Strategy1TicketModel ticket) {
        OrderModel sellOrder = orderService.createLimitQuantityOrder(
                ticket.getSymbol(),
                String.valueOf(ticket.getBoughtQuantity()),
                OrderSide.SELL,
                TimeInForce.GTC,
                String.valueOf(ticket.getAverageFilledBuyPrice() * (1 + (gainPercentage / 100))));
        ticket.setSellOrderId(sellOrder.getId());
        ticket.setStatus(Strategy1TicketStatus.SELL_UNFILLED);
        strategy1TicketRepository.save(ticket);
        logger.info("Successfully created a SELL order for {} ticket", ticket.getSymbol());
    }

    public Strategy1TicketModel orderToTicket(OrderModel order) {
        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
        if (ticket != null) {
            return ticket;
        } else {
            return new Strategy1TicketModel(order);
        }
    }

    //@Scheduled(cron = "${STRATEGY_1_UPDATE_CRON}")
    public void updateUncompletedTickets() {
        logger.info("Updating tickets...");
        List<Strategy1TicketModel> uncompletedTickets = strategy1TicketRepository.findUncompletedTickets();
        uncompletedTickets.forEach(this::updateUncompletedTicket);
        logger.info("Update done!");
    }

    private void updateUncompletedTicket(Strategy1TicketModel strategy1TicketModel) {
        // TODO
    }

    public void processFilledOrder(OrderModel order) {
        switch (order.getSide()) {
            case "buy" -> processFilledBuyOrder(order);
            case "sell" -> processFilledSellOrder(order);
        }
    }

    private void processFilledBuyOrder(OrderModel order) {
        logger.info("[\uD83D\uDCB8] Bought ${} worth of {}!", notional, order.getSymbol().split("/")[0]);
        Strategy1TicketModel strategy1TicketModel = new Strategy1TicketModel(order);
        createSellOrder(strategy1TicketModel);
    }

    private void processFilledSellOrder(OrderModel order) {
        logger.info("Processing a filled SELL {} order", order.getSymbol());
        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
        ticket.setStatus(Strategy1TicketStatus.COMPLETE);
        ticket.setSoldQuantity(order.getFilledQuantity());
        ticket.setAverageFilledSellPrice(order.getFilledAvgPrice());
        strategy1TicketRepository.save(ticket);
        logger.info("This {} ticket is now completed!", ticket.getSymbol());
    }
}
