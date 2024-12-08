package fr.flolec.alpacabot.alpacaapi.bar;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.RestClientConfiguration;
import fr.flolec.alpacabot.alpacaapi.bar.historicalbar.HistoricalBarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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

    public static final String BARS_RESPONSE_BODY_10_AAL = "{\"bars\":{\"AAL\":[{\"c\":17.7399,\"h\":18.09,\"l\":17.4201,\"n\":22074,\"o\":17.45,\"t\":\"2024-12-06T14:00:00Z\",\"v\":7245623,\"vw\":17.798133},{\"c\":17.44,\"h\":17.5,\"l\":17.1,\"n\":794,\"o\":17.29,\"t\":\"2024-12-06T13:00:00Z\",\"v\":276041,\"vw\":17.352715},{\"c\":17.28,\"h\":17.35,\"l\":17.21,\"n\":175,\"o\":17.34,\"t\":\"2024-12-06T12:00:00Z\",\"v\":42269,\"vw\":17.275872},{\"c\":17.35,\"h\":17.4,\"l\":17.24,\"n\":110,\"o\":17.25,\"t\":\"2024-12-06T11:00:00Z\",\"v\":21280,\"vw\":17.343176},{\"c\":17.26,\"h\":17.26,\"l\":17.22,\"n\":63,\"o\":17.26,\"t\":\"2024-12-06T10:00:00Z\",\"v\":8180,\"vw\":17.242922},{\"c\":17.26,\"h\":17.3,\"l\":17.24,\"n\":143,\"o\":17.28,\"t\":\"2024-12-06T09:00:00Z\",\"v\":36875,\"vw\":17.259885},{\"c\":17.3299,\"h\":17.34,\"l\":17.26,\"n\":154,\"o\":17.3,\"t\":\"2024-12-06T00:00:00Z\",\"v\":47057,\"vw\":17.318109},{\"c\":17.29,\"h\":17.33,\"l\":17.28,\"n\":190,\"o\":17.32,\"t\":\"2024-12-05T23:00:00Z\",\"v\":46696,\"vw\":17.306364},{\"c\":17.29,\"h\":17.35,\"l\":17.26,\"n\":296,\"o\":17.34,\"t\":\"2024-12-05T22:00:00Z\",\"v\":74130,\"vw\":17.309976},{\"c\":17.34,\"h\":17.63,\"l\":17.31,\"n\":745,\"o\":17.38,\"t\":\"2024-12-05T21:00:00Z\",\"v\":3466054,\"vw\":17.396012}]},\"next_page_token\":\"QUFMfE18MTczMzQzMjQwMDAwMDAwMDAwMA==\"}";

    @Value("${ALPACA_DATA_HISTORICAL_BARS_URI_CRYPTO}")
    private String uriCrypto;

    @Value("${ALPACA_DATA_HISTORICAL_BARS_URI_STOCKS}")
    private String uriStocks;

    @Value("${MAX_BARS_PER_SYMBOL}")
    private int maxBarsPerSymbol = 10;

    @Autowired
    private HistoricalBarService historicalBarService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @MockBean
    private BarModelRepository barModelRepository;


    @Test
    void getHistoricalBars_noPageToken_onePageRetrieved() throws AlpacaApiException, JsonProcessingException {
        mockRestServiceServer.expect(requestTo(startsWith(uriCrypto)))
                .andExpect(queryParam("symbols", "AAVE%2FUSD"))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_NO_TOKEN, MediaType.APPLICATION_JSON));

        List<BarModel> barModels = historicalBarService.getHistoricalBars("AAVE/USD", BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK, true);

        assertNotNull(barModels);
        assertEquals(7, barModels.size());

        BarModel barModel = barModels.get(0);
        assertEquals("AAVE/USD", barModel.getSymbol());
        assertEquals(97.85955, barModel.getClose());
        assertEquals(99.95425, barModel.getHigh());
        assertEquals(85.04845, barModel.getLow());
        assertEquals(87.1966, barModel.getOpen());
        assertEquals(360.569480264, barModel.getVolume());
        assertEquals(Instant.parse("2024-05-20T05:00:00Z"), barModel.getBeginTime());
    }

    @Test
    void getHistoricalBars_withPageToken_nextPageRetrieved() throws AlpacaApiException, JsonProcessingException {
        mockRestServiceServer.expect(requestTo(startsWith(uriCrypto)))
                .andExpect(queryParam("symbols", "AAVE%2FUSD"))
                .andExpect(queryParam("page_token", ""))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_WITH_TOKEN, MediaType.APPLICATION_JSON));
        mockRestServiceServer.expect(requestTo(startsWith(uriCrypto)))
                .andExpect(queryParam("symbols", "AAVE%2FUSD"))
                .andExpect(queryParam("page_token", "AZERTY1234567890"))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_NO_TOKEN, MediaType.APPLICATION_JSON));

        List<BarModel> barModels = historicalBarService.getHistoricalBars("AAVE/USD", BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK, true);

        assertNotNull(barModels);
        assertEquals(14, barModels.size());
    }

    @Test
    void getHistoricalBars_httpError_throwAlpacaApiException() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(queryParam("symbols", "AAVE%2FUSD"))
                .andExpect(queryParam("page_token", ""))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Forbidden\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        AlpacaApiException alpacaApiException = assertThrows(AlpacaApiException.class, () -> historicalBarService.getHistoricalBars("AAVE/USD", BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK, true));
        assertEquals(HttpStatus.FORBIDDEN, alpacaApiException.getStatusCode());
        assertEquals("{\"message\":\"Forbidden\"}", alpacaApiException.getAlpacaErrorMessage());
        assertEquals("Historical bars for 'AAVE/USD' could not be retrieved", alpacaApiException.getCustomMessage());
    }

    @Test
    void getNumberOfHourlyBars_nominal_requestSentAndDeserializedResults() throws AlpacaApiException, JsonProcessingException {
        mockRestServiceServer.expect(requestTo(startsWith(uriStocks)))
                .andExpect(queryParam("symbols", "AAL"))
                .andExpect(queryParam("timeframe", "1Hour"))
                .andExpect(queryParam("limit", "10"))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_10_AAL, MediaType.APPLICATION_JSON));

        List<BarModel> barModels = historicalBarService.getNumberOfHourlyBars("AAL", 10);

        assertNotNull(barModels);
        assertEquals(10, barModels.size());

        BarModel barModel = barModels.get(0);
        assertEquals("AAL", barModel.getSymbol());
        assertEquals(17.7399, barModel.getClose());
        assertEquals(18.09, barModel.getHigh());
        assertEquals(17.4201, barModel.getLow());
        assertEquals(17.45, barModel.getOpen());
        assertEquals(7245623, barModel.getVolume());
        assertEquals(Instant.parse("2024-12-06T14:00:00Z"), barModel.getBeginTime());
    }

    @Test
    void getNumberOfHourlyBars_httpError_throwAlpacaApiException() {
        mockRestServiceServer.expect(requestTo(startsWith(uriStocks)))
                .andExpect(queryParam("symbols", "AAL"))
                .andExpect(queryParam("timeframe", "1Hour"))
                .andExpect(queryParam("limit", "10"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Forbidden\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        AlpacaApiException alpacaApiException = assertThrows(AlpacaApiException.class, () -> historicalBarService.getNumberOfHourlyBars("AAL", 10));
        assertEquals(HttpStatus.FORBIDDEN, alpacaApiException.getStatusCode());
        assertEquals("{\"message\":\"Forbidden\"}", alpacaApiException.getAlpacaErrorMessage());
        assertEquals("Historical bars for 'AAL' could not be retrieved", alpacaApiException.getCustomMessage());
    }

    @Test
    void loadHistoricalBars_nominal_repositoryCalled() throws AlpacaApiException, JsonProcessingException {
        HistoricalBarService spiedHistoricalBarService = spy(historicalBarService);
        BarModel barModel1 = new BarModel("2024-12-06T14:00:00Z", 17.73, 18.09, 17.42, 17.45, 724623);
        BarModel barModel2 = new BarModel("2024-12-06T13:00:00Z", 17.44, 17.52, 17.12, 17.29, 276041);
        doReturn(List.of(barModel1, barModel2)).when(spiedHistoricalBarService).getNumberOfHourlyBars("AAL", 2);

        List<String> assets = spiedHistoricalBarService.loadHistoricalBars("AAL", 2);

        verify(barModelRepository, times(2)).insertOrReplace(any());
        verify(barModelRepository).cleanExcessBars("AAL", maxBarsPerSymbol);
        assertEquals(List.of("AAL"), assets);
    }

    @Test
    void deleteAll_nominal_repositoryCalled() {
        historicalBarService.deleteAll();
        verify(barModelRepository).deleteAll();
    }

    @Test
    void barTimeFrameFromLabel_nominal_enumMatched() {
        assertEquals(BarTimeFrame.HOUR3, BarTimeFrame.fromLabel("3Hour"));
    }

    @Test
    void barTimeFrameFromLabel_wrongLabel_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> BarTimeFrame.fromLabel("wrong label"));
    }

    @Test
    void periodLengthUnitFromLabel_nominal_enumMatched() {
        assertEquals(PeriodLengthUnit.DAY, PeriodLengthUnit.fromLabel("Day"));
    }

    @Test
    void periodLengthUnitFromLabel_wrongLabel_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> PeriodLengthUnit.fromLabel("wrong label"));
    }

    @Test
    void periodLengthUnitGoBackInTime_nominalForEachPeriod_newDateCorrect() {
        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(2024, 12, 25, 13, 54), ZoneOffset.ofHours(2));
        assertEquals("2024-12-25T13:44:00+02:00", PeriodLengthUnit.MIN.goBackInTime(start, 10));
        assertEquals("2024-12-25T10:54:00+02:00", PeriodLengthUnit.HOUR.goBackInTime(start, 3));
        assertEquals("2024-12-20T13:54:00+02:00", PeriodLengthUnit.DAY.goBackInTime(start, 5));
        assertEquals("2024-12-18T13:54:00+02:00", PeriodLengthUnit.WEEK.goBackInTime(start, 1));
        assertEquals("2024-08-25T13:54:00+02:00", PeriodLengthUnit.MONTH.goBackInTime(start, 4));
    }

}