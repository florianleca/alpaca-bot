package fr.flolec.alpacabot.strategies.strategy1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class Strategy1Test {

    @Autowired
    private Strategy1 strategy1;

    @Test
    void checkBuyOpportunities() throws IOException {
        strategy1.checkBuyOpportunities();
    }
}