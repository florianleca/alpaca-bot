package fr.flolec.alpacabot.indicators;

import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MomentumIndicatorTest {

    private final int lengthKC = 20;

    private MomentumIndicator momentumIndicator;

    private BarSeries barSeries;

    @BeforeEach
    void setUp() throws IOException {
        barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/bars.csv");
        momentumIndicator = new MomentumIndicator(barSeries, lengthKC);
    }

    @Test
    void calculate() {
        Map<ZonedDateTime, Num> values = new HashMap<>();
        for (int i = 2 * lengthKC; i < barSeries.getBarCount(); i++) {
            values.put(barSeries.getBar(i).getEndTime(), momentumIndicator.getValue(i));
        }
        int delta = 20;
        int[] expectedValues = {1170, 1193, 1208, 1203, 1155, 1110, 969, 663, 354, -119, -526, -827, -1103, -1305, -1477, -1588};
        ZonedDateTime startDate = ZonedDateTime.parse("2024-07-29T07:00:00Z");
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], values.get(startDate.plusHours(i)).doubleValue(), delta);
        }
    }

    @Test
    void getUnstableBars() {
        assertEquals(20, momentumIndicator.getUnstableBars());
    }

}