package fr.flolec.alpacabot.strategies.strategy2;

import fr.flolec.alpacabot.strategies.utils.ChartBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.ta4j.core.BarSeries;

import java.io.IOException;

@SpringBootTest
class Strategy2ServiceTest {

    @Autowired
    private Strategy2Service strategy2Service;

    @Autowired
    private ChartBuilder chartBuilder;

    @Test
    void test() throws IOException {
        BarSeries bars = strategy2Service.getLastHourBars("BTC/USD");
        System.out.println(bars.getFirstBar().getBeginTime());
        System.out.println(bars.getFirstBar().getEndTime());
        System.out.println(bars.getLastBar().getBeginTime());
        System.out.println(bars.getLastBar().getEndTime());
    }

    @BeforeEach
    public void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void testChart() throws IOException, InterruptedException {
        BarSeries bars = strategy2Service.getLastHourBars("BTC/USD");
        chartBuilder.run(strategy2Service.buildStrategy2(bars), bars);
        Thread.sleep(10000000);
    }

}