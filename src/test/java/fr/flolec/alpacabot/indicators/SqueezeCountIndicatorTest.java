package fr.flolec.alpacabot.indicators;

import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqueezeCountIndicatorTest {

    private SqueezeCountIndicator squeezeCountIndicator;
    private BarSeries barSeries;

    @BeforeEach
    void setUp() throws IOException {
        barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/bars.csv");
        SqueezeIndicator squeezeIndicator = new SqueezeIndicator(barSeries, 20, 1.5, 20, 1.5);
        squeezeCountIndicator = new SqueezeCountIndicator(squeezeIndicator);
    }

    @Test
    void calculate() {
        Map<ZonedDateTime, Integer> values = new HashMap<>();
        for(int i = 0; i < barSeries.getBarCount(); i++) {
            values.put(barSeries.getBar(i).getEndTime(), squeezeCountIndicator.getValue(i));
        }
        assertEquals(0, values.get(ZonedDateTime.parse("2024-07-28T23:00Z")));
        assertEquals(1, values.get(ZonedDateTime.parse("2024-07-29T00:00Z")));
        assertEquals(2, values.get(ZonedDateTime.parse("2024-07-29T01:00Z")));
        assertEquals(33, values.get(ZonedDateTime.parse("2024-07-30T08:00Z")));
        assertEquals(0, values.get(ZonedDateTime.parse("2024-07-30T09:00Z")));
    }

    @Test
    void getUnstableBars() {
        assertEquals(20, squeezeCountIndicator.getUnstableBars());
    }

}