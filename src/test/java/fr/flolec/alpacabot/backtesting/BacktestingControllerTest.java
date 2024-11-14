package fr.flolec.alpacabot.backtesting;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.strategies.StrategyEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.HttpClientErrorException;

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
    private MockHttpServletRequestBuilder request;

    @BeforeEach
    void setUp() {
        request = get("/backtesting/SQUEEZE_MOMENTUM")
                .queryParam("symbol", "TEST/USD")
                .queryParam("periodLength", "1")
                .queryParam("periodLengthUnit", "MONTH")
                .queryParam("timeFrame", "HOUR1")
                .queryParam("isCrypto", "true");
    }

    @Test
    void backtesting_nominal_serviceCalled() throws Exception {
        when(backtestingService.backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1, true)).thenReturn(null);

        this.mockMvc.perform(request).andDo(print())
                .andExpect(status().isOk());

        verify(backtestingService).backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1, true);
    }

    @Test
    void backtesting_httpError_errorStatusTransfered() throws Exception {
        HttpClientErrorException httpError = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        AlpacaApiException alpacaApiException = new AlpacaApiException(httpError, "customMessage");
        when(backtestingService.backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1, true))
                .thenThrow(alpacaApiException);

        this.mockMvc.perform(request).andDo(print())
                .andExpect(status().isBadRequest());

        verify(backtestingService).backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1, true);
    }

    @Test
    void backtesting_jsonParsingError_badGatewayStatus() throws Exception {
        when(backtestingService.backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1, true))
                .thenThrow(JsonProcessingException.class);

        this.mockMvc.perform(request).andDo(print())
                .andExpect(status().isBadGateway());

        verify(backtestingService).backtesting(StrategyEnum.SQUEEZE_MOMENTUM, "TEST/USD", 1, PeriodLengthUnit.MONTH, BarTimeFrame.HOUR1, true);
    }

}