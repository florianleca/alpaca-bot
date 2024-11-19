package fr.flolec.alpacabot.backtesting;

import fr.flolec.alpacabot.alpacaapi.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.alpacaapi.bar.historicalbar.HistoricalBarService;
import fr.flolec.alpacabot.strategies.StrategyEnum;
import fr.flolec.alpacabot.strategies.squeezemomentum.SqueezeMomentumStrategyBuilder;
import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BacktestingServiceTest {

    @Mock
    private HistoricalBarService historicalBarService;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private BacktestingService backtestingService;

    @Test
    void backtesting() throws Exception {
        when(historicalBarService.getHistoricalBars(any(), any(), anyLong(), any(), anyBoolean())).thenReturn(new ArrayList<>());
        when(applicationContext.getBean(SqueezeMomentumStrategyBuilder.class)).thenReturn(new SqueezeMomentumStrategyBuilder(20, 2.0, 20, 1.5, 6));

        BacktestResult backtestResult = backtestingService.backtesting(
                StrategyEnum.SQUEEZE_MOMENTUM,
                "TEST/USD",
                1,
                PeriodLengthUnit.MONTH,
                BarTimeFrame.HOUR1,
                true);

        verify(historicalBarService).getHistoricalBars("TEST/USD", BarTimeFrame.HOUR1, 1, PeriodLengthUnit.MONTH, true);
        verify(applicationContext).getBean(SqueezeMomentumStrategyBuilder.class);
        assertNotNull(backtestResult);
    }

    @Test
    void backtestingResult() throws IOException {
        // Given
        BarSeries series = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1h-2024-08.csv", Duration.ofHours(1));
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.enter(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1));
        tradingRecord.exit(1, DecimalNum.valueOf(150), DecimalNum.valueOf(1));
        tradingRecord.enter(2);
        tradingRecord.exit(3);
        tradingRecord.enter(4);
        tradingRecord.exit(5);

        // When
        BacktestResult backtestResult = new BacktestResult(series, tradingRecord);

        // Then
        assertNotNull(backtestResult);
        assertEquals(3, backtestResult.getPositions().size());
        BacktestResult.PositionSummary positionSummary = backtestResult.getPositions().get(0);
        assertEquals(ZonedDateTime.parse("2024-08-01T00:00:00.000Z[UTC]"), positionSummary.getEntryDate());
        assertEquals(100, positionSummary.getEntryPrice());
        assertEquals(ZonedDateTime.parse("2024-08-01T01:00:00.000Z[UTC]"), positionSummary.getExitDate());
        assertEquals(150, positionSummary.getExitPrice());
        assertEquals(50, positionSummary.getProfit());
        assertTrue(backtestResult.getRoi() > 1);
    }

}