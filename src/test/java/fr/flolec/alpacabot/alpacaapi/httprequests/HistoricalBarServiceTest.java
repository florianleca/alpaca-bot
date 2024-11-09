package fr.flolec.alpacabot.alpacaapi.httprequests;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.historicalbar.HistoricalBarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(HistoricalBarService.class)
@ContextConfiguration(classes = {
        RestClientConfiguration.class,
        HistoricalBarService.class})
class HistoricalBarServiceTest {

    public static final String BARS_RESPONSE_BODY_NO_TOKEN = "{\"bars\":{\"AAVE/USD\":[{\"c\":97.85955,\"h\":99.95425,\"l\":85.04845,\"n\":13,\"o\":87.1966,\"t\":\"2024-05-20T05:00:00Z\",\"v\":360.569480264,\"vw\":86.7279312981},{\"c\":96.235,\"h\":99.779,\"l\":94.916,\"n\":7,\"o\":97.748,\"t\":\"2024-05-21T05:00:00Z\",\"v\":8.791924485,\"vw\":97.022854293},{\"c\":95.70095,\"h\":97.2915,\"l\":92.94355,\"n\":5,\"o\":96.2712,\"t\":\"2024-05-22T05:00:00Z\",\"v\":5.824661152,\"vw\":94.7906871946},{\"c\":101.546,\"h\":104.69,\"l\":91.1113,\"n\":3,\"o\":95.6748,\"t\":\"2024-05-23T05:00:00Z\",\"v\":0.108950264,\"vw\":96.6121514971},{\"c\":107.3635,\"h\":107.75,\"l\":95.2195,\"n\":2,\"o\":101.4075,\"t\":\"2024-05-24T05:00:00Z\",\"v\":0.176735907,\"vw\":96.9437093521},{\"c\":105.102,\"h\":108.45,\"l\":103.657,\"n\":6,\"o\":107.265,\"t\":\"2024-05-25T05:00:00Z\",\"v\":6.044406515,\"vw\":105.7681871454},{\"c\":106.5015,\"h\":110.279,\"l\":104.3425,\"n\":2,\"o\":105.15,\"t\":\"2024-05-26T05:00:00Z\",\"v\":1.09107443,\"vw\":108.2569519123}]},\"next_page_token\":null}\n";
    public static final String BARS_RESPONSE_BODY_WITH_TOKEN = "{\"bars\":{\"AAVE/USD\":[{\"c\":97.85955,\"h\":99.95425,\"l\":85.04845,\"n\":13,\"o\":87.1966,\"t\":\"2024-05-20T05:00:00Z\",\"v\":360.569480264,\"vw\":86.7279312981},{\"c\":96.235,\"h\":99.779,\"l\":94.916,\"n\":7,\"o\":97.748,\"t\":\"2024-05-21T05:00:00Z\",\"v\":8.791924485,\"vw\":97.022854293},{\"c\":95.70095,\"h\":97.2915,\"l\":92.94355,\"n\":5,\"o\":96.2712,\"t\":\"2024-05-22T05:00:00Z\",\"v\":5.824661152,\"vw\":94.7906871946},{\"c\":101.546,\"h\":104.69,\"l\":91.1113,\"n\":3,\"o\":95.6748,\"t\":\"2024-05-23T05:00:00Z\",\"v\":0.108950264,\"vw\":96.6121514971},{\"c\":107.3635,\"h\":107.75,\"l\":95.2195,\"n\":2,\"o\":101.4075,\"t\":\"2024-05-24T05:00:00Z\",\"v\":0.176735907,\"vw\":96.9437093521},{\"c\":105.102,\"h\":108.45,\"l\":103.657,\"n\":6,\"o\":107.265,\"t\":\"2024-05-25T05:00:00Z\",\"v\":6.044406515,\"vw\":105.7681871454},{\"c\":106.5015,\"h\":110.279,\"l\":104.3425,\"n\":2,\"o\":105.15,\"t\":\"2024-05-26T05:00:00Z\",\"v\":1.09107443,\"vw\":108.2569519123}]},\"next_page_token\":\"AZERTY1234567890\"}\n";


    @Value("${ALPACA_DATA_HISTORICAL_BARS_URI_CRYPTO}")
    private String uri;

    @Autowired
    private HistoricalBarService historicalBarService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    @DisplayName("getHistoricalBars: no page token -> one page retrieved")
    void getHistoricalBars_noPageToken_onePageRetrieved() {
        mockRestServiceServer.expect(requestTo(startsWith(uri)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_NO_TOKEN, MediaType.APPLICATION_JSON));

        List<BarModel> barModels = historicalBarService.getHistoricalBars("AAVE/USD", BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK, true);

        assertNotNull(barModels);
        assertEquals(7, barModels.size());

        BarModel barModel = barModels.get(0);
        assertEquals(97.85955, barModel.getClose());
        assertEquals(99.95425, barModel.getHigh());
        assertEquals(85.04845, barModel.getLow());
        assertEquals(87.1966, barModel.getOpen());
        assertEquals(360.569480264, barModel.getVolume());
        assertEquals("2024-05-20T05:00:00Z", barModel.getDate());
    }

    @Test
    @DisplayName("getHistoricalBars: with page token -> next page is retrieved")
    void getHistoricalBars_withPageToken_nextPageRetrieved() {
        mockRestServiceServer.expect(requestTo(startsWith(uri)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andExpect(queryParam("page_token", ""))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_WITH_TOKEN, MediaType.APPLICATION_JSON));
        mockRestServiceServer.expect(requestTo(startsWith(uri)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andExpect(queryParam("page_token", "AZERTY1234567890"))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_NO_TOKEN, MediaType.APPLICATION_JSON));

        List<BarModel> barModels = historicalBarService.getHistoricalBars("AAVE/USD", BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK, true);

        assertNotNull(barModels);
        assertEquals(14, barModels.size());
    }

    @Test
    @DisplayName("getHistoricalBars: error -> empty list & logged error")
    void getHistoricalBars_error_emptyListAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andExpect(queryParam("page_token", ""))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Forbidden\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        List<BarModel> barModels = historicalBarService.getHistoricalBars("AAVE/USD", BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK, true);

        assertNotNull(barModels);
        assertTrue(barModels.isEmpty());
    }

    @Test
    @DisplayName("BarTimeFrame.fromLabel: nominal -> enum matched")
    void barTimeFrameFromLabel_nominal_enumMatched() {
        assertEquals(BarTimeFrame.HOUR3, BarTimeFrame.fromLabel("3Hour"));
    }

    @Test
    @DisplayName("BarTimeFrame.fromLabel: wrong label -> exception thrown")
    void barTimeFrameFromLabel_wrongLabel_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> BarTimeFrame.fromLabel("wrong label"));
    }

    @Test
    @DisplayName("PeriodLengthUnit.fromLabel: nominal -> enum matched")
    void periodLengthUnitFromLabel_nominal_enumMatched() {
        assertEquals(PeriodLengthUnit.DAY, PeriodLengthUnit.fromLabel("Day"));
    }

    @Test
    @DisplayName("PeriodLengthUnit.fromLabel: wrong label -> exception thrown")
    void periodLengthUnitFromLabel_wrongLabel_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> PeriodLengthUnit.fromLabel("wrong label"));
    }

    @Test
    @DisplayName("PeriodLengthUnit.goBackInTime: nominal for each period -> new date correct")
    void periodLengthUnitGoBackInTime_nominalForEachPeriod_newDateCorrect() {
        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(2024, 12, 25, 13, 54), ZoneOffset.ofHours(2));
        assertEquals("2024-12-25T13:44:00+02:00", PeriodLengthUnit.MIN.goBackInTime(start, 10));
        assertEquals("2024-12-25T10:54:00+02:00", PeriodLengthUnit.HOUR.goBackInTime(start, 3));
        assertEquals("2024-12-20T13:54:00+02:00", PeriodLengthUnit.DAY.goBackInTime(start, 5));
        assertEquals("2024-12-18T13:54:00+02:00", PeriodLengthUnit.WEEK.goBackInTime(start, 1));
        assertEquals("2024-08-25T13:54:00+02:00", PeriodLengthUnit.MONTH.goBackInTime(start, 4));
    }

}