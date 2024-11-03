package fr.flolec.alpacabot.alpacaapi.httprequests;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LatestQuoteService.class)
@ContextConfiguration(classes = {
        RestClientConfiguration.class,
        LatestQuoteService.class})
class LatestQuoteServiceTest {

    public static final String LATEST_QUOTE_RESPONSE_BODY_EXAMPLE = "{\"quotes\":{\"AAVE/USD\":{\"ap\":110.5,\"as\":37.89,\"bp\":110.17,\"bs\":38.247,\"t\":\"2024-05-26T17:00:40.913476723Z\"}}}\n";

    @Value("${ALPACA_DATA_LATEST_QUOTES_URI}")
    private String uri;

    @Autowired
    private LatestQuoteService latestQuoteService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    @DisplayName("The latest quote of an asset is retrieved correctly")
    void getLatestQuote() {
        mockRestServiceServer.expect(requestTo(startsWith(uri)))
                .andExpect(queryParam("symbols", "AAVE/USD"))
                .andRespond(withSuccess(LATEST_QUOTE_RESPONSE_BODY_EXAMPLE, MediaType.APPLICATION_JSON));

        AssetModel asset = new AssetModel();
        asset.setSymbol("AAVE/USD");
        double latestQuote = latestQuoteService.getLatestQuote(asset);
        assertEquals(110.5, latestQuote);
    }

}