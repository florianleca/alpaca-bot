package fr.flolec.alpacabot.strategies.squeezemomentum;

import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqueezeMomentumStrategyBuilderTest {

    private SqueezeMomentumStrategyBuilder squeezeMomentumStrategyBuilder;

    @BeforeEach
    void setUp() {
        squeezeMomentumStrategyBuilder = new SqueezeMomentumStrategyBuilder(20, 1.5, 20, 1.5, 6);
    }

    @Test
    void buildStrategy3() throws IOException {

        BarSeries series = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1h-2024-08.csv", Duration.ofHours(1));
        Strategy strategy = squeezeMomentumStrategyBuilder.buildStrategy(series);

        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        int positionCount = tradingRecord.getPositionCount();
        assertEquals(3, positionCount);

        List<Integer> indexEntry = Arrays.asList(305, 456, 499);
        List<Integer> indexExit = Arrays.asList(313, 464, 504);

        for (int i = 0; i < positionCount; i++) {
            assertEquals(indexEntry.get(i), tradingRecord.getPositions().get(i).getEntry().getIndex() - 1);
            assertEquals(indexExit.get(i), tradingRecord.getPositions().get(i).getExit().getIndex() - 1);
        }
    }

}