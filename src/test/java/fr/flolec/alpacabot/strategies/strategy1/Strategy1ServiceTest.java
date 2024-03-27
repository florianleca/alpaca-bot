package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionService;
import fr.flolec.alpacabot.alpacaapi.websocket.AlpacaWebSocket;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

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

    @Value("${PREVIOUSLY_BOUGHT_PERCENTAGE}")
    private double previouslyBoughtPercentage;

    @BeforeAll
    @AfterAll
    static void setUpTearDown(@Autowired PositionService positionService,
                      @Autowired Strategy1TicketRepository strategy1TicketRepository) {
        positionService.liquidateAllPositions();
        strategy1TicketRepository.deleteAll();
    }

    @BeforeEach
    void closeSocket() {
        alpacaWebSocket.closeSocket();
    }

    @AfterEach
    void openSocket() {
        alpacaWebSocket.openSocket();
    }

//    @Test
//    @DisplayName("Create and update ticket - instant filled order")
//    void orderAssetAndCreateTicket() throws IOException {
//        orderService.cancelAllOrders();
//        double positionQtyBeforeBuyOrder = positionService.getCurrentQtyOfAsset("BTC/USD");
//        OrderModel order = orderService.createMarketNotionalOrder(
//                "BTC/USD",
//                "1",
//                OrderSide.BUY,
//                TimeInForce.IOC);
//        assertNotNull(order);
//        // L'ordre doit être 'pending_new'
//        assertEquals("pending_new", order.getStatus());
//        // On crée le ticket avec cet ordre
//        strategy1Service.createTicket(order, positionQtyBeforeBuyOrder);
//        // Le ticket doit être en BD
//        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
//        assertNotNull(ticket);
//        assertEquals(order.getId(), ticket.getBuyOrderId());
//        // Le websocket étant fermé, le statut reste forcément à BUY_UNFILLED
//        assertEquals(Strategy1TicketStatus.BUY_UNFILLED, ticket.getStatus());
//        // Test de 'orderToTicket' (cas nominal)
//        Strategy1TicketModel ticket2 = strategy1Service.orderToTicket(order);
//        assertEquals(ticket.getBuyOrderId(), ticket2.getBuyOrderId());
//    }
//
//    @Test
//    @DisplayName("Create and update ticket - instant canceled order")
//    void orderAssetAndCreateTicketCanceled() throws IOException, InterruptedException {
//        orderService.cancelAllOrders();
//        double positionQtyBeforeBuyOrder = positionService.getCurrentQtyOfAsset("BTC/USD");
//        OrderModel order = orderService.createLimitNotionalOrder(
//                "BTC/USD",
//                "1",
//                OrderSide.BUY,
//                TimeInForce.IOC,
//                "10");
//        Thread.sleep(250);
//        order = orderService.getOrderById(order.getId());
//        assertNotNull(order);
//        // L'ordre doit être 'canceled'
//        assertEquals("canceled", order.getStatus());
//        // On tente de créer le ticket, mais une exception est levée
//        OrderModel orderLambda = order;
//        assertThrows(IllegalArgumentException.class, () -> strategy1Service.createTicket(orderLambda, positionQtyBeforeBuyOrder));
//        // Le ticket ne doit pas être en BD
//        Strategy1TicketModel ticket = strategy1TicketRepository.findByOrder(order.getId());
//        assertNull(ticket);
//        // Test de 'orderToTicket' (cas d'échec)
//        assertThrows(RuntimeException.class, () -> strategy1Service.orderToTicket(orderLambda));
//    }

}