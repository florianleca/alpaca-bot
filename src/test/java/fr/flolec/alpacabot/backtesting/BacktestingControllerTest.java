package fr.flolec.alpacabot.backtesting;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.strategies.StrategyEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BacktestingControllerTest {

    @Mock
    private BacktestingService backtestingService;

    @Mock
    private BacktestResult backtestResult;

    @InjectMocks
    private BacktestingController backtestingController;

    @Test
    void backtesting() throws IOException {
        when(backtestingService.backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1)).thenReturn(backtestResult);
        BacktestResult result = backtestingController.backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1);
        verify(backtestingService).backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1);
        assertEquals(backtestResult, result);
    }
}