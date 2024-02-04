package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(classes = AlpacaBotApplication.class)
public class Strategy1ServiceTest {

    @Autowired
    private Strategy1Service strategy1;

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Launching Strategy 1 opportunity checker")
    void checkBuyOpportunities() throws IOException {
        strategy1.checkBuyOpportunities();
    }

    @Test
    @DisplayName("")
    void statusOfIOCorder() throws IOException {
        OrderModel buyOrder = orderService.createLimitNotionalOrder(
                "BTC/USD",
                "1",
                OrderSide.BUY,
                TimeInForce.IOC,
                "2000"
        );
        System.out.println(buyOrder.getStatus());
        System.out.println(buyOrder.getFilledAt());
        System.out.println(" ");
        buyOrder = orderService.getOrderById(buyOrder.getId());
        System.out.println(buyOrder.getStatus());
        System.out.println(buyOrder.getFilledAt());
        System.out.println(" ");
    }
}