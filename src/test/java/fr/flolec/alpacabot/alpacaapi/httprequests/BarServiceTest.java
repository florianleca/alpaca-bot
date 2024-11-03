package fr.flolec.alpacabot.alpacaapi.httprequests;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BarService.class)
@ContextConfiguration(classes = {
        RestClientConfiguration.class,
        BarService.class})
class BarServiceTest {

    public static final String BARS_RESPONSE_BODY_NO_TOKEN = "{\"bars\":{\"AAVE/USD\":[{\"c\":97.85955,\"h\":99.95425,\"l\":85.04845,\"n\":13,\"o\":87.1966,\"t\":\"2024-05-20T05:00:00Z\",\"v\":360.569480264,\"vw\":86.7279312981},{\"c\":96.235,\"h\":99.779,\"l\":94.916,\"n\":7,\"o\":97.748,\"t\":\"2024-05-21T05:00:00Z\",\"v\":8.791924485,\"vw\":97.022854293},{\"c\":95.70095,\"h\":97.2915,\"l\":92.94355,\"n\":5,\"o\":96.2712,\"t\":\"2024-05-22T05:00:00Z\",\"v\":5.824661152,\"vw\":94.7906871946},{\"c\":101.546,\"h\":104.69,\"l\":91.1113,\"n\":3,\"o\":95.6748,\"t\":\"2024-05-23T05:00:00Z\",\"v\":0.108950264,\"vw\":96.6121514971},{\"c\":107.3635,\"h\":107.75,\"l\":95.2195,\"n\":2,\"o\":101.4075,\"t\":\"2024-05-24T05:00:00Z\",\"v\":0.176735907,\"vw\":96.9437093521},{\"c\":105.102,\"h\":108.45,\"l\":103.657,\"n\":6,\"o\":107.265,\"t\":\"2024-05-25T05:00:00Z\",\"v\":6.044406515,\"vw\":105.7681871454},{\"c\":106.5015,\"h\":110.279,\"l\":104.3425,\"n\":2,\"o\":105.15,\"t\":\"2024-05-26T05:00:00Z\",\"v\":1.09107443,\"vw\":108.2569519123}]},\"next_page_token\":null}\n";
    public static final String BARS_RESPONSE_BODY_WITH_TOKEN = "{\"bars\":{\"AAVE/USD\":[{\"c\":97.85955,\"h\":99.95425,\"l\":85.04845,\"n\":13,\"o\":87.1966,\"t\":\"2024-05-20T05:00:00Z\",\"v\":360.569480264,\"vw\":86.7279312981},{\"c\":96.235,\"h\":99.779,\"l\":94.916,\"n\":7,\"o\":97.748,\"t\":\"2024-05-21T05:00:00Z\",\"v\":8.791924485,\"vw\":97.022854293},{\"c\":95.70095,\"h\":97.2915,\"l\":92.94355,\"n\":5,\"o\":96.2712,\"t\":\"2024-05-22T05:00:00Z\",\"v\":5.824661152,\"vw\":94.7906871946},{\"c\":101.546,\"h\":104.69,\"l\":91.1113,\"n\":3,\"o\":95.6748,\"t\":\"2024-05-23T05:00:00Z\",\"v\":0.108950264,\"vw\":96.6121514971},{\"c\":107.3635,\"h\":107.75,\"l\":95.2195,\"n\":2,\"o\":101.4075,\"t\":\"2024-05-24T05:00:00Z\",\"v\":0.176735907,\"vw\":96.9437093521},{\"c\":105.102,\"h\":108.45,\"l\":103.657,\"n\":6,\"o\":107.265,\"t\":\"2024-05-25T05:00:00Z\",\"v\":6.044406515,\"vw\":105.7681871454},{\"c\":106.5015,\"h\":110.279,\"l\":104.3425,\"n\":2,\"o\":105.15,\"t\":\"2024-05-26T05:00:00Z\",\"v\":1.09107443,\"vw\":108.2569519123}]},\"next_page_token\":\"AZERTY1234567890\"}\n";


    @Value("${ALPACA_DATA_BARS_URI}")
    private String uri;

    @Autowired
    private BarService barService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    @DisplayName("Historical bars are retrieved and serialized correctly")
    void getHistoricalBarsNoToken() {
        mockRestServiceServer.expect(requestTo(startsWith(uri)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_NO_TOKEN, MediaType.APPLICATION_JSON));

        List<BarModel> barModels = barService.getHistoricalBars("AAVE/USD", BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK);

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
    @DisplayName("GIVEN a page token WHEN getHistoricalBars is called THEN the next page is retrieved")
    void getHistoricalBarsWithToken() {

        mockRestServiceServer.expect(requestTo(startsWith(uri)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andExpect(queryParam("page_token", ""))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_WITH_TOKEN, MediaType.APPLICATION_JSON));

        mockRestServiceServer.expect(requestTo(startsWith(uri)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andExpect(queryParam("page_token", "AZERTY1234567890"))
                .andRespond(withSuccess(BARS_RESPONSE_BODY_NO_TOKEN, MediaType.APPLICATION_JSON));

        List<BarModel> barModels = barService.getHistoricalBars("AAVE/USD", BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK);

        assertNotNull(barModels);
        assertEquals(14, barModels.size());
    }

    @Test
    @DisplayName("BarTimeFrame nominal and error casting from labels")
    void barTimeFrameFromLabel() {
        assertEquals(BarTimeFrame.HOUR3, BarTimeFrame.fromLabel("3Hour"));
        assertThrows(IllegalArgumentException.class, () -> BarTimeFrame.fromLabel("wrong label"));
    }

    @Test
    @DisplayName("PeriodLengthUnit nominal and error casting from labels")
    void periodLengthUnitFromLabel() {
        assertEquals(PeriodLengthUnit.DAY, PeriodLengthUnit.fromLabel("Day"));
        assertThrows(IllegalArgumentException.class, () -> PeriodLengthUnit.fromLabel("wrong label"));
    }

    @Test
    @DisplayName("Each goBackInTime method in PeriodLengthUnit is correct")
    void periodLengthUnitGoBackInTime() {
        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(2024, 12, 25, 13, 54), ZoneOffset.ofHours(2));
        assertEquals("2024-12-25T13:44:00+02:00", PeriodLengthUnit.MIN.goBackInTime(start, 10));
        assertEquals("2024-12-25T10:54:00+02:00", PeriodLengthUnit.HOUR.goBackInTime(start, 3));
        assertEquals("2024-12-20T13:54:00+02:00", PeriodLengthUnit.DAY.goBackInTime(start, 5));
        assertEquals("2024-12-18T13:54:00+02:00", PeriodLengthUnit.WEEK.goBackInTime(start, 1));
        assertEquals("2024-08-25T13:54:00+02:00", PeriodLengthUnit.MONTH.goBackInTime(start, 4));
    }

}