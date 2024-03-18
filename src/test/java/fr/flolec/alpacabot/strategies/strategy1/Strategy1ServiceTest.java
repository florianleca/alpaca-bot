package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionService;
import fr.flolec.alpacabot.alpacaapi.websocket.AlpacaWebSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AlpacaBotApplication.class)
public class Strategy1ServiceTest {

    @Autowired
    private Strategy1Service strategy1Service;

    @Autowired
    private Strategy1TicketRepository strategy1TicketRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private AlpacaWebSocket alpacaWebSocket;

    @Value("${THRESHOLD}")
    private double threshold;

    @Value("${PREVIOUSLY_BOUGHT_PERCENTAGE}")
    private double previouslyBoughtPercentage;

    @BeforeEach
    void closeSocket() {
        alpacaWebSocket.closeSocket();
    }

    @AfterEach
    void openSocket() {
        alpacaWebSocket.openSocket();
    }

    @Test
    @DisplayName("Launching Strategy 1 opportunity checker")
    void checkBuyOpportunities() throws IOException {
        strategy1Service.checkBuyOpportunities();
    }

    @Test
    @DisplayName("Create and update ticket - instant filled order")
    void orderAssetAndCreateTicket() throws IOException {
        orderService.cancelAllOrders();
        double positionQtyBeforeBuyOrder = positionService.getCurrentQtyOfAsset("BTC/USD");
        OrderModel order = orderService.createMarketNotionalOrder(
                "BTC/USD",
                "1",
                OrderSide.BUY,
                TimeInForce.IOC);
        assertNotNull(order);
        // L'ordre doit être 'pending_new'
        assertEquals("pending_new", order.getStatus());
        // On crée le ticket avec cet ordre
        strategy1Service.createTicket(order, positionQtyBeforeBuyOrder);
        // Le ticket doit être en BD
        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
        assertNotNull(ticket);
        assertEquals(order.getId(), ticket.getBuyOrderId());
        // CreateTicket appelant updateTicket, le statut a dû passer à BUY_FILLED_SELL_UNFILLED
        assertEquals(Strategy1TicketStatus.BUY_FILLED_SELL_UNFILLED, ticket.getStatus());
        // Test de 'orderToTicket' (cas nominal)
        Strategy1TicketModel ticket2 = strategy1Service.orderToTicket(order);
        assertEquals(ticket.getBuyOrderId(), ticket2.getBuyOrderId());
    }

    @Test
    @DisplayName("Create and update ticket - instant canceled order")
    void orderAssetAndCreateTicketCanceled() throws IOException, InterruptedException {
        orderService.cancelAllOrders();
        double positionQtyBeforeBuyOrder = positionService.getCurrentQtyOfAsset("BTC/USD");
        OrderModel order = orderService.createLimitNotionalOrder(
                "BTC/USD",
                "1",
                OrderSide.BUY,
                TimeInForce.IOC,
                "10");
        Thread.sleep(250);
        order = orderService.getOrderById(order.getId());
        assertNotNull(order);
        // L'ordre doit être 'canceled'
        assertEquals("canceled", order.getStatus());
        // On tente de créer le ticket, mais une exception est levée
        OrderModel orderLambda = order;
        assertThrows(IllegalArgumentException.class, () -> strategy1Service.createTicket(orderLambda, positionQtyBeforeBuyOrder));
        // Le ticket ne doit pas être en BD
        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
        assertNull(ticket);
        // Test de 'orderToTicket' (cas d'échec)
        assertThrows(RuntimeException.class, () -> strategy1Service.orderToTicket(orderLambda));
    }

    @Test
    @DisplayName("Asset decreasing percent below and above threshold")
    void decreasedMoreThanThreshold() {
        AssetModel asset = new AssetModel();
        asset.setSymbol("TEST/TEST");
        asset.setName("Testing");
        asset.setLatestValue(100);
        double maxHighSame = 100;
        double maxHighThreshold = 100 / (1 - (threshold / 100));
        double maxHighMoreThanThreshold = 150;
        assertFalse(strategy1Service.decreasedMoreThanThreshold(asset, maxHighSame));
        assertTrue(strategy1Service.decreasedMoreThanThreshold(asset, maxHighThreshold));
        assertTrue(strategy1Service.decreasedMoreThanThreshold(asset, maxHighMoreThanThreshold));
        assertThrows(RuntimeException.class, () -> strategy1Service.decreasedMoreThanThreshold(asset, 99));
    }

    @Test
    @DisplayName("Minimum buyPrice value of list of tickets is returned")
    void minBuyPriceFromTicketList() {
        Strategy1TicketModel ticket1 = new Strategy1TicketModel();
        ticket1.setAverageFilledBuyPrice(100);
        Strategy1TicketModel ticket2 = new Strategy1TicketModel();
        ticket2.setAverageFilledBuyPrice(200);
        List<Strategy1TicketModel> tickets = new ArrayList<>();
        assertEquals(-1, strategy1Service.minBuyPriceFromTicketList(tickets));
        tickets.add(ticket2);
        assertEquals(200, strategy1Service.minBuyPriceFromTicketList(tickets));
        tickets.add(ticket1);
        assertEquals(100, strategy1Service.minBuyPriceFromTicketList(tickets));
    }

}