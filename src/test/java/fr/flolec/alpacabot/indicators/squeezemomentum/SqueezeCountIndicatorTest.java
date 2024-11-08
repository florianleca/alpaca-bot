package fr.flolec.alpacabot.indicators.squeezemomentum;

import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqueezeCountIndicatorTest {

    private SqueezeCountIndicator squeezeCountIndicator;
    private BarSeries barSeries;

    @BeforeEach
    void setUp() throws IOException {
        barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1h-2024-08.csv", Duration.ofHours(1));
        SqueezeIndicator squeezeIndicator = new SqueezeIndicator(barSeries, 20, 1.5, 20, 1.5);
        squeezeCountIndicator = new SqueezeCountIndicator(squeezeIndicator);
    }

    @Test
    void calculate() {
        List<Integer> values = new ArrayList<>();
        for(int i = 0; i < barSeries.getBarCount(); i++) {
            values.add(squeezeCountIndicator.getValue(i));
        }

        // Valeurs stables
        for(int i = 20; i <= 35; i++) assertEquals(0, values.get(i));
        assertEquals(1, values.get(36));
        assertEquals(6, values.get(41));
        for(int i = 42; i <= 59; i++) assertEquals(0, values.get(i));
        assertEquals(1, values.get(60));
        assertEquals(6, values.get(65));
    }

    @Test
    void getUnstableBars() {
        assertEquals(20, squeezeCountIndicator.getUnstableBars());
    }

}