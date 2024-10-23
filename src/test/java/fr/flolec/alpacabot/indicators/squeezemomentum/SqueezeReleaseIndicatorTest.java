package fr.flolec.alpacabot.indicators.squeezemomentum;

import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqueezeReleaseIndicatorTest {

    private SqueezeReleaseIndicator squeezeReleaseIndicator;

    private BarSeries barSeries;

    @BeforeEach
    void setUp() throws IOException {
        barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1h-2024-08.csv", Duration.ofHours(1));
        SqueezeIndicator squeezeIndicator = new SqueezeIndicator(barSeries, 20, 1.5, 20, 1.5);
        SqueezeCountIndicator squeezeCountIndicator = new SqueezeCountIndicator(squeezeIndicator);
        squeezeReleaseIndicator = new SqueezeReleaseIndicator(squeezeCountIndicator, 6);
    }

    @Test
    void calculate() {
        List<Integer> expected = new ArrayList<>(Arrays.asList(42, 66, 88, 122, 163, 256, 305, 328, 352, 387, 431, 456, 499, 539, 638, 686));

        // Valeurs instables
        for (int i = 0; i <= 19; i++) assertFalse(squeezeReleaseIndicator.getValue(i));

        // Valeurs stables
        for (int i = 20; i < barSeries.getBarCount(); i++) {
            if (expected.contains(i)) assertTrue(squeezeReleaseIndicator.getValue(i));
            else assertFalse(squeezeReleaseIndicator.getValue(i));
        }
    }

    @Test
    void getUnstableBars() {
        assertEquals(20, squeezeReleaseIndicator.getUnstableBars());
    }

}