package fr.flolec.alpacabot;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.strategies.strategy1.Strategy1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AlpacaBotApplicationRunner implements ApplicationRunner {

    private final Strategy1Service strategy1Service;

    @Autowired
    public AlpacaBotApplicationRunner(Strategy1Service strategy1Service) {
        this.strategy1Service = strategy1Service;
    }

    @Override
    public void run(ApplicationArguments args) {
        strategy1Service.applicationBootingSearchingForUpdates();
    }

}
