package fr.flolec.alpacabot.order;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Buying $1 of BTC on market")
    void createMarketNotionalOrder() throws IOException {
        OrderModel order = orderService.createMarketNotionalOrder(
                "BTC/USD",
                "1",
                OrderSide.BUY,
                TimeInForce.GTC);
        assertEquals("buy", order.getSide());
        assertEquals("market", order.getType());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1", order.getNotional());
        assertNull(order.getFilledAt());
        order = orderService.getOrderById(order.getId());
        assertNotNull(order.getFilledAt());
    }

    @Test
    @DisplayName("Buying $1 of BTC on market for $100k/coin")
    void createLimitNotionalOrder() throws IOException {
        OrderModel order = orderService.createLimitNotionalOrder(
                "BTC/USD",
                "1",
                OrderSide.BUY,
                TimeInForce.GTC,
                "100000");
        assertEquals("buy", order.getSide());
        assertEquals("limit", order.getType());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1", order.getNotional());
        assertEquals(100000, order.getLimitPrice());
        assertNull(order.getFilledAt());
        order = orderService.getOrderById(order.getId());
        assertNotNull(order.getFilledAt());
    }

}