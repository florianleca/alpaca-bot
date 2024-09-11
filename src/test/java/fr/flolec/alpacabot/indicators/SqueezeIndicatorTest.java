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

class SqueezeIndicatorTest {

    private SqueezeIndicator squeezeIndicator;
    private BarSeries barSeries;

    @BeforeEach
    void setUp() throws IOException {
        barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/bars.csv");
        squeezeIndicator = new SqueezeIndicator(barSeries, 20, 1.5, 20, 1.5);
    }

    @Test
    void calculate() {
        Map<ZonedDateTime, Boolean> values = new HashMap<>();
        for(int i = 0; i < barSeries.getBarCount(); i++) {
            values.put(barSeries.getBar(i).getEndTime(), squeezeIndicator.getValue(i));
        }

        assertEquals(true, values.get(ZonedDateTime.parse("2024-07-28T23:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-07-29T00:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-07-30T04:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-07-30T08:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-07-30T09:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-07-31T01:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-07-31T20:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-07-31T21:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-08-01T05:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-08-01T13:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-08-01T14:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-08-01T16:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-08-02T11:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-08-02T13:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-08-02T14:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-08-02T15:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-08-02T16:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-08-02T18:00:00Z")));
        assertEquals(false, values.get(ZonedDateTime.parse("2024-08-03T11:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-08-03T13:00:00Z")));
        assertEquals(true, values.get(ZonedDateTime.parse("2024-08-03T14:00:00Z")));
    }

    @Test
    void getUnstableBars() {
        assertEquals(20, squeezeIndicator.getUnstableBars());
    }

}