package fr.flolec.alpacabot.alpacaapi.httprequests.order;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.*;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionService;
import fr.flolec.alpacabot.alpacaapi.websocket.AlpacaWebSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AlpacaBotApplication.class)
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LatestQuoteService latestQuoteService;

    @Autowired
    private AlpacaWebSocket alpacaWebSocket;

    @BeforeEach
    void closeSocket() {
        alpacaWebSocket.closeSocket();
    }

    @AfterEach
    void openSocket() {
        alpacaWebSocket.openSocket();
    }

    @Test
    @DisplayName("Buying $1.27 of LINK on market")
    void createMarketNotionalOrder() throws IOException {
        OrderModel order = orderService.createMarketNotionalOrder(
                "LINK/USD",
                "1.27",
                OrderSide.BUY,
                TimeInForce.GTC);
        assertEquals("buy", order.getSide());
        assertEquals("market", order.getOrderType());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals("LINK/USD", order.getSymbol());
        assertEquals("1.27", order.getNotional());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(0, order.getFilledAvgPrice());
        assertEquals(0, order.getLimitPrice());
        assertEquals("pending_new", order.getStatus());
        assertNull(order.getQuantity());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    @DisplayName("Buying $1 of BTC on market for latest price")
    void createLimitNotionalOrder() throws IOException {
        double price = latestQuoteService.getLatestQuote("BTC/USD");
        OrderModel order = orderService.createLimitNotionalOrder(
                "BTC/USD",
                "1",
                OrderSide.BUY,
                TimeInForce.GTC,
                String.valueOf(price));
        assertEquals("buy", order.getSide());
        assertEquals("limit", order.getOrderType());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1", order.getNotional());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(0, order.getFilledAvgPrice());
        assertEquals(price, order.getLimitPrice());
        assertEquals("pending_new", order.getStatus());
        assertNull(order.getQuantity());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    @DisplayName("Buying 0.0001 BTC on market for latest price")
    void createLimitQuantityOrder() throws IOException {
        double price = latestQuoteService.getLatestQuote("BTC/USD");
        OrderModel order = orderService.createLimitQuantityOrder(
                "BTC/USD",
                "0.0001",
                OrderSide.BUY,
                TimeInForce.GTC,
                String.valueOf(price));
        assertEquals("buy", order.getSide());
        assertEquals("limit", order.getOrderType());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(0, order.getFilledAvgPrice());
        assertEquals(price, order.getLimitPrice());
        assertEquals("pending_new", order.getStatus());
        assertEquals("0.0001", order.getQuantity());
        assertNull(order.getNotional());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertNotNull(order.getCreatedAt());
    }

//    @Test
//    @DisplayName("Attribut 'positionQtyBeforeOrder' is set when ordering")
//    void setPositionQtyBeforeOrder() throws IOException, InterruptedException {
//        positionService.liquidatePositionByPercentage("UNI/USD", 100);
//        Thread.sleep(500);
//        OrderModel order = orderService.createMarketNotionalOrder(
//                "UNI/USD",
//                "1",
//                OrderSide.BUY,
//                TimeInForce.GTC);
//        assertEquals(0, order.getPositionQtyBeforeOrder());
//        Thread.sleep(250);
//        order = orderService.createMarketNotionalOrder(
//                "UNI/USD",
//                "1",
//                OrderSide.BUY,
//                TimeInForce.GTC);
//        assertTrue(order.getPositionQtyBeforeOrder() > 0);
//    }

    @Test
    @DisplayName("Fill order from web socket messages")
    void fillOrder() throws InterruptedException, IOException {
        // Précaution pour éviter possibles problèmes
        orderService.cancelAllOrders();
        long bdSize = orderRepository.count();
        assertEquals(0, orderService.countUnfilledBuyOrder("BTC/USD"));
        // Passer un ordre et l'archiver en BD
        OrderModel createdBuyOrder = orderService.createMarketNotionalOrder(
                "BTC/USD",
                "1.32",
                OrderSide.BUY,
                TimeInForce.GTC);
        Thread.sleep(250);
        orderService.archive(createdBuyOrder);
        assertEquals(1, orderService.countUnfilledBuyOrder("BTC/USD"));
        // Cet ordre doit être en BD avec filled_at = null
        assertEquals(bdSize + 1, orderRepository.count());
        OrderModel unfilledBuyOrder = orderRepository.findById(createdBuyOrder.getId()).orElse(null);
        assertNotNull(unfilledBuyOrder);
        assertNull(unfilledBuyOrder.getFilledAt());
        // On simule la réception d'un message de fill
        String message = "{\"stream\":\"trade_updates\",\"data\":{\"event\":\"fill\",\"timestamp\":\"2024-01-28T13:35:00.985937652Z\",\"order\":{\"id\":\""
                + createdBuyOrder.getId()
                + "\",\"client_order_id\":\"79e9fae0-9cdc-43b5-b563-f2cc9161a847\",\"created_at\":\"2024-01-28T13:24:05.684793892Z\",\"updated_at\":\"2024-01-28T13:35:00.991327011Z\",\"submitted_at\":\"2024-01-28T13:24:05.683511142Z\",\"filled_at\":\"2024-01-28T13:35:00.985937652Z\",\"expired_at\":null,\"cancel_requested_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"f0a05db3-5c93-4524-8a32-2f2b8d4f12fc\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":\"1.32\",\"qty\":null,\"filled_qty\":\"0.004152306\",\"filled_avg_price\":\"240.83\",\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"time_in_force\":\"gtc\",\"limit_price\":\"240.83\",\"stop_price\":null,\"status\":\"filled\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null},\"price\":\"240.83\",\"qty\":\"0.004152306\",\"position_qty\":\"0.045574602\",\"execution_id\":\"a9da1e63-279d-489b-9e5f-a753dd4f0d9f\"}}";
        orderService.fillOrder(message);
        // L'ordre buy doit être en BD avec la date de fill donnée
        OrderModel filledBuyOrder = orderRepository.findById(createdBuyOrder.getId()).orElse(null);
        assertNotNull(filledBuyOrder);
        assertEquals(createdBuyOrder.getId(), filledBuyOrder.getId());
        assertEquals(Date.from(Instant.parse("2024-01-28T13:35:00.985937652Z")), filledBuyOrder.getFilledAt());
        assertEquals(0, orderService.countUnfilledBuyOrder("BTC/USD"));
        // On le supprime de la BD
        orderRepository.deleteById(createdBuyOrder.getId());
        assertEquals(bdSize, orderRepository.count());
    }

    @Test
    @DisplayName("Fill order with bad message")
    void fillOrderBadMessage() {
        assertThrows(RuntimeException.class, () -> orderService.fillOrder("Not a real filling message"));
    }

    @Test
    @DisplayName("Update of unfilled orders in DB")
    void updateUnfilledOrders() throws IOException, InterruptedException {
        // Passer un ordre d'achat au marché
        OrderModel createdBuyOrder = orderService.createMarketNotionalOrder(
                "BTC/USD",
                "1.32",
                OrderSide.BUY,
                TimeInForce.GTC);
        Thread.sleep(250);
        orderService.archive(createdBuyOrder);
        // Il doit être unfilled en BD
        OrderModel unfilledBuyOrder = orderRepository.findById(createdBuyOrder.getId()).orElse(null);
        assertNotNull(unfilledBuyOrder);
        assertNull(unfilledBuyOrder.getFilledAt());
        // Update
        orderService.updateUnfilledOrders();
        // S'assurer que le statut est passé à filled
        OrderModel filledBuyOrder = orderRepository.findById(createdBuyOrder.getId()).orElse(null);
        assertNotNull(filledBuyOrder);
        assertNotNull(filledBuyOrder.getFilledAt());
    }
}