package fr.flolec.alpacabot.indicators.squeezemomentum;

import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MomentumIndicatorTest {

    private final int lengthKC = 20;

    private MomentumIndicator momentumIndicator;

    private BarSeries barSeries;

    @BeforeEach
    void setUp() throws IOException {
        barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1h-2024-08.csv", Duration.ofHours(1));
        momentumIndicator = new MomentumIndicator(barSeries, lengthKC);
    }

    @Test
    void calculate() {
        List<Num> values = new ArrayList<>();
        for (int i = 0; i < barSeries.getBarCount(); i++) values.add(momentumIndicator.getValue(i));

        // Valeurs instables
        for (int i = 0; i < 2 * lengthKC; i++) {
            assertNull(values.get(i));
        }

        // Après 40mn (2 * lengthKC) les valeurs sont stables
        double[] expectedValues = {-400.14, -608.66, -792.00, -964.43, -1118.84, -1337.80, -1649.85, -1867.97, -2095.79, -2251.58};
        for (int i = 0; i < expectedValues.length; i++) {
            BigDecimal bd = new BigDecimal(Double.toString(values.get(i + 2 * lengthKC).doubleValue()));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            double calculatedValue = bd.doubleValue();
            assertEquals(expectedValues[i], calculatedValue);
        }
    }

    @Test
    void getUnstableBars() {
        assertEquals(40, momentumIndicator.getUnstableBars());
    }

}