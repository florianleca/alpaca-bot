package fr.flolec.alpacabot.alpacaapi.bar;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.RestClientConfiguration;
import fr.flolec.alpacabot.alpacaapi.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.bar.latestbar.LatestBarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LatestBarService.class)
@ContextConfiguration(classes = {
        RestClientConfiguration.class,
        LatestBarService.class})
class LatestBarServiceTest {

    public static final String LATEST_BAR_CRYPTO_RESPONSE_BODY = "{\"bars\":{\"AAVE/USD\":{\"c\":182.8055,\"h\":182.8055,\"l\":182.8055,\"n\":0,\"o\":182.8055,\"t\":\"2024-11-08T12:11:00Z\",\"v\":0,\"vw\":0}}}";
    public static final String LATEST_BAR_STOCKS_RESPONSE_BODY = "{\"bars\":{\"AAL\":{\"c\":13.615,\"h\":13.62,\"l\":13.6,\"n\":177,\"o\":13.605,\"t\":\"2024-11-07T20:59:00Z\",\"v\":50272,\"vw\":13.608698}}}";
    public static final String LATEST_BAR_ERROR_BODY_EXAMPLE = "{\"bars\":{}}";

    @Value("${ALPACA_DATA_LATEST_BARS_URI_CRYPTO}")
    private String uriCrypto;

    @Value("${ALPACA_DATA_LATEST_BARS_URI_STOCKS}")
    private String uriStocks;

    @Autowired
    private LatestBarService latestBarService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    void getLatestBarCloseValue_crypto_latestBarRetrieved() throws AlpacaApiException, JsonProcessingException {
        mockRestServiceServer.expect(requestTo(startsWith(uriCrypto)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andRespond(withSuccess(LATEST_BAR_CRYPTO_RESPONSE_BODY, MediaType.APPLICATION_JSON));
        AssetModel asset = new AssetModel();
        asset.setSymbol("AAVE/USD");

        double latestBar = latestBarService.getLatestBarCloseValue(asset, true);

        assertEquals(182.8055, latestBar);
    }

    @Test
    void getLatestBarCloseValue_stocks_latestBarRetrieved() throws AlpacaApiException, JsonProcessingException {
        mockRestServiceServer.expect(requestTo(startsWith(uriStocks)))
                .andExpect(queryParam("symbols", "AAL"))
                .andRespond(withSuccess(LATEST_BAR_STOCKS_RESPONSE_BODY, MediaType.APPLICATION_JSON));
        AssetModel asset = new AssetModel();
        asset.setSymbol("AAL");

        double latestBar = latestBarService.getLatestBarCloseValue(asset, false);

        assertEquals(13.615, latestBar);
    }

    @Test
    void getLatestBarCloseValue_httpError_throwAlpacaApiException() {
        mockRestServiceServer.expect(requestTo(startsWith(uriCrypto)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .body(LATEST_BAR_ERROR_BODY_EXAMPLE)
                        .contentType(MediaType.APPLICATION_JSON));
        AssetModel asset = new AssetModel();
        asset.setSymbol("AAVE/USD");

        AlpacaApiException alpacaApiException = assertThrows(AlpacaApiException.class, () -> latestBarService.getLatestBarCloseValue(asset, true));
        assertEquals(HttpStatus.FORBIDDEN, alpacaApiException.getStatusCode());
        assertEquals(LATEST_BAR_ERROR_BODY_EXAMPLE, alpacaApiException.getAlpacaErrorMessage());
        assertEquals("Latest bar of AAVE/USD could not be retrieved", alpacaApiException.getCustomMessage());
    }

}