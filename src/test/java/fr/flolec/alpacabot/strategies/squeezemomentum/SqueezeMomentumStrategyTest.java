package fr.flolec.alpacabot.strategies.squeezemomentum;

import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqueezeMomentumStrategyTest {

    private SqueezeMomentumStrategy squeezeMomentumStrategy;

    @BeforeEach
    void setUp() {
        squeezeMomentumStrategy = new SqueezeMomentumStrategy(20, 1.5, 20, 1.5, 6);
    }

    @Test
    void buildStrategy3() throws IOException {

        BarSeries series = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1h-2024-08.csv", Duration.ofHours(1));
        Strategy strategy = squeezeMomentumStrategy.buildStrategy(series);

        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        tradingRecord.getPositions().forEach(p -> {
            ZonedDateTime entry = series.getBar(p.getEntry().getIndex() -1).getBeginTime();
            ZonedDateTime exit = series.getBar(p.getExit().getIndex()-1).getBeginTime();
            System.out.println(entry + " : " + (p.getEntry().getIndex() - 1) + " / " + exit + " : " + (p.getExit().getIndex() - 1));
        });

        int positionCount = tradingRecord.getPositionCount();
        assertEquals(3, positionCount);

        List<Integer> indexEntry = Arrays.asList(305, 456, 499);
        List<Integer> indexExit = Arrays.asList(313, 464, 504);

        for (int i = 0; i < positionCount; i++) {
            assertEquals(indexEntry.get(i), tradingRecord.getPositions().get(i).getEntry().getIndex() - 1);
            assertEquals(indexExit.get(i), tradingRecord.getPositions().get(i).getExit().getIndex() - 1);
        }

        var criterion = new ReturnCriterion();
        Num num = criterion.calculate(series, tradingRecord);
        System.out.println("Return: " + num);
    }

}

/*
squeeze release alone :
2024-08-02T18:00Z[UTC] : 42 / 2024-08-02T19:00Z[UTC] : 43
2024-08-03T18:00Z[UTC] : 66 / 2024-08-03T19:00Z[UTC] : 67
2024-08-04T16:00Z[UTC] : 88 / 2024-08-04T17:00Z[UTC] : 89
2024-08-06T02:00Z[UTC] : 122 / 2024-08-06T06:00Z[UTC] : 126
2024-08-07T19:00Z[UTC] : 163 / 2024-08-07T20:00Z[UTC] : 164
2024-08-11T16:00Z[UTC] : 256 / 2024-08-11T17:00Z[UTC] : 257
2024-08-13T17:00Z[UTC] : 305 / 2024-08-14T01:00Z[UTC] : 313
2024-08-14T16:00Z[UTC] : 328 / 2024-08-14T17:00Z[UTC] : 329
2024-08-15T16:00Z[UTC] : 352 / 2024-08-15T18:00Z[UTC] : 354
2024-08-17T03:00Z[UTC] : 387 / 2024-08-17T04:00Z[UTC] : 388
2024-08-18T23:00Z[UTC] : 431 / 2024-08-19T00:00Z[UTC] : 432
2024-08-20T00:00Z[UTC] : 456 / 2024-08-20T08:00Z[UTC] : 464
2024-08-21T19:00Z[UTC] : 499 / 2024-08-22T00:00Z[UTC] : 504
2024-08-23T11:00Z[UTC] : 539 / 2024-08-23T13:00Z[UTC] : 541
2024-08-27T14:00Z[UTC] : 638 / 2024-08-27T15:00Z[UTC] : 639
2024-08-29T14:00Z[UTC] : 686 / 2024-08-29T16:00Z[UTC] : 688


squeeze release + lime green momentum :
2024-08-06T02:00Z[UTC] : 122 / 2024-08-06T06:00Z[UTC] : 126
2024-08-13T17:00Z[UTC] : 305 / 2024-08-14T01:00Z[UTC] : 313
2024-08-15T16:00Z[UTC] : 352 / 2024-08-15T18:00Z[UTC] : 354
2024-08-20T00:00Z[UTC] : 456 / 2024-08-20T08:00Z[UTC] : 464
2024-08-21T19:00Z[UTC] : 499 / 2024-08-22T00:00Z[UTC] : 504
2024-08-29T14:00Z[UTC] : 686 / 2024-08-29T16:00Z[UTC] : 688

avec tout (sixSqOn.and(limeGreen).and(above200EMA)) :
2024-08-13T17:00Z[UTC] : 305 / 2024-08-14T01:00Z[UTC] : 313
2024-08-20T00:00Z[UTC] : 456 / 2024-08-20T08:00Z[UTC] : 464
2024-08-21T19:00Z[UTC] : 499 / 2024-08-22T00:00Z[UTC] : 504

 */