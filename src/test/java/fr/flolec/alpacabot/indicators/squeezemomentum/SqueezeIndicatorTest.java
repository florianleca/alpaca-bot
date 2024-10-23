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
import static org.junit.jupiter.api.Assertions.assertNull;

class SqueezeIndicatorTest {

    private SqueezeIndicator squeezeIndicator;
    private BarSeries barSeries;

    @BeforeEach
    void setUp() throws IOException {
        barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1m-2024-09-21.csv", Duration.ofMinutes(1));
        squeezeIndicator = new SqueezeIndicator(barSeries, 20, 1.5, 20, 1.5);
    }

    @Test
    void calculate() {
        List<Boolean> values = new ArrayList<>();
        for(int i = 0; i < barSeries.getBarCount(); i++) {
            values.add(squeezeIndicator.getValue(i));
        }

        // Valeurs instables
        for(int i = 0; i <= 19; i++) assertNull(values.get(i));

        // Après 20mn (lengthBB/KC) les valeurs sont stables
        for(int i = 20; i <= 37; i++) assertEquals(false, values.get(i));
        for(int i = 38; i <= 40; i++) assertEquals(true, values.get(i));
        for(int i = 41; i <= 113; i++) assertEquals(false, values.get(i));
        for(int i = 114; i <= 117; i++) assertEquals(true, values.get(i));
        for(int i = 118; i <= 125; i++) assertEquals(false, values.get(i));
        assertEquals(true, values.get(126));
        assertEquals(false, values.get(127));
        for(int i = 128; i <= 147; i++) assertEquals(true, values.get(i));
        for(int i = 148; i <= 168; i++) assertEquals(false, values.get(i));
        assertEquals(true, values.get(169));
    }

    @Test
    void getUnstableBars() {
        assertEquals(20, squeezeIndicator.getUnstableBars());
    }

}