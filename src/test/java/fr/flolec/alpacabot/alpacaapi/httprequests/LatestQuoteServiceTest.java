package fr.flolec.alpacabot.alpacaapi.httprequests;

import fr.flolec.alpacabot.WireMockedTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LatestQuoteServiceTest extends WireMockedTest {

    public final static String LATEST_QUOTE_RESPONSE_BODY_EXAMPLE = "{\"quotes\":{\"AAVE/USD\":{\"ap\":110.5,\"as\":37.89,\"bp\":110.17,\"bs\":38.247,\"t\":\"2024-05-26T17:00:40.913476723Z\"}}}\n";

    private LatestQuoteService latestQuoteService;

    @BeforeEach
    public void setUp() {
        latestQuoteService = new LatestQuoteService("http://localhost:8080/latest/quotes", objectMapper, httpRequestService);
        // Configurer la réponse simulée
        stubFor(get(urlPathEqualTo("/latest/quotes"))
                .withQueryParam("symbols", equalTo("AAVE/USD"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(LATEST_QUOTE_RESPONSE_BODY_EXAMPLE)));
    }

    @Test
    @DisplayName("The latest quote of an asset is retrieved correctly")
    void getLatestQuote() throws IOException {
        AssetModel asset = new AssetModel();
        asset.setSymbol("AAVE/USD");
        double latestQuote = latestQuoteService.getLatestQuote(asset);
        assertEquals(110.5, latestQuote);
    }

}