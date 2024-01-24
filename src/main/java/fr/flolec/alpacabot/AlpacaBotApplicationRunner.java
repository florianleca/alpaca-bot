package fr.flolec.alpacabot;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AlpacaBotApplicationRunner implements ApplicationRunner {

    @Autowired
    OrderService orderService;

    @Override
    public void run(ApplicationArguments args) {
        orderService.updateUnfilledOrders();
    }

}
