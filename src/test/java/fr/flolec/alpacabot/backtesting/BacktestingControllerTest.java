package fr.flolec.alpacabot.backtesting;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.strategies.StrategyEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BacktestingController.class)
class BacktestingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BacktestingService backtestingService;


    @Test
    void backtesting_get_serviceCalled() throws Exception {
        when(backtestingService.backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1, true)).thenReturn(null);

        this.mockMvc.perform(get("/backtesting/SQUEEZE_MOMENTUM")
                        .queryParam("symbol", "TEST/USD")
                        .queryParam("periodLength", "1")
                        .queryParam("periodLengthUnit", "MONTH")
                        .queryParam("timeFrame", "HOUR1")
                        .queryParam("isCrypto", "true"))
                .andDo(print()).andExpect(status().isOk());

        verify(backtestingService).backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1, true);
    }

}