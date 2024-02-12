package fr.flolec.alpacabot.alpacaapi.httprequests.order;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import fr.flolec.alpacabot.alpacaapi.websocket.AlpacaWebSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AlpacaBotApplication.class)
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

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
    void createMarketNotionalOrder() {
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
        getOrderById(order);
    }

    void getOrderById(OrderModel order) {
        OrderModel retrievedOrder = orderService.getOrderById(order.getId());
        assertNotNull(retrievedOrder);
        assertEquals(order.getId(), retrievedOrder.getId());
        assertEquals(order.getCreatedAt(), retrievedOrder.getCreatedAt());
    }

    @Test
    @DisplayName("Buying $1 of BTC on market for not enough money")
    void createLimitNotionalOrder() throws IOException {
        //double price = latestQuoteService.getLatestQuote("BTC/USD");
        OrderModel order = orderService.createLimitNotionalOrder(
                "BTC/USD",
                "1",
                OrderSide.BUY,
                TimeInForce.GTC,
                // String.valueOf(price))
                "10");
        assertEquals("buy", order.getSide());
        assertEquals("limit", order.getOrderType());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1", order.getNotional());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(0, order.getFilledAvgPrice());
        assertEquals(10, order.getLimitPrice());
        assertEquals("pending_new", order.getStatus());
        assertNull(order.getQuantity());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertNotNull(order.getCreatedAt());
        cancelAllOrders(order);
    }

    void cancelAllOrders(OrderModel order) throws IOException {
        orderService.cancelAllOrders();
        order = orderService.getOrderById(order.getId());
        assertEquals("canceled", order.getStatus());
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

    @Test
    @DisplayName("messageToOrder with good and bad messages")
    void messageToOrder() {
        String goodMessage = "{\"stream\":\"trade_updates\",\"data\":{\"event\":\"fill\",\"timestamp\":\"2024-01-28T13:35:00.985937652Z\",\"order\":{\"id\":\"azerty123456789\",\"client_order_id\":\"79e9fae0-9cdc-43b5-b563-f2cc9161a847\",\"created_at\":\"2024-01-28T13:24:05.684793892Z\",\"updated_at\":\"2024-01-28T13:35:00.991327011Z\",\"submitted_at\":\"2024-01-28T13:24:05.683511142Z\",\"filled_at\":\"2024-01-28T13:35:00.985937652Z\",\"expired_at\":null,\"cancel_requested_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"f0a05db3-5c93-4524-8a32-2f2b8d4f12fc\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":\"1.32\",\"qty\":null,\"filled_qty\":\"0.004152306\",\"filled_avg_price\":\"240.83\",\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"time_in_force\":\"gtc\",\"limit_price\":\"240.83\",\"stop_price\":null,\"status\":\"filled\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null},\"price\":\"240.83\",\"qty\":\"0.004152306\",\"position_qty\":\"0.045574602\",\"execution_id\":\"a9da1e63-279d-489b-9e5f-a753dd4f0d9f\"}}";
        String badMessage = "bad message";
        OrderModel order = orderService.messageToOrder(goodMessage);
        assertNotNull(order);
        assertEquals("azerty123456789", order.getId());
        assertEquals("BTC/USD", order.getSymbol());
        assertThrows(RuntimeException.class, () -> orderService.messageToOrder(badMessage));
    }

}