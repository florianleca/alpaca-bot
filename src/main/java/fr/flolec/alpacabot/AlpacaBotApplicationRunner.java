package fr.flolec.alpacabot;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AlpacaBotApplicationRunner implements ApplicationRunner {

    private final OrderService orderService;

    @Autowired
    public AlpacaBotApplicationRunner(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(ApplicationArguments args) {
        orderService.updateUnfilledOrders();
    }

}
