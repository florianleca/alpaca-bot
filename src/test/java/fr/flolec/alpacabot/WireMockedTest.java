package fr.flolec.alpacabot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import org.springframework.test.util.ReflectionTestUtils;

@WireMockTest(httpPort = 8080)
public abstract class WireMockedTest {

    protected final ObjectMapper objectMapper;
    protected final HttpRequestService httpRequestService;

    public WireMockedTest() {
        objectMapper = new ObjectMapper();
        httpRequestService = new HttpRequestService();// Utilisation réelle du service HTTP
        ReflectionTestUtils.setField(httpRequestService, "keyId", "toto");
        ReflectionTestUtils.setField(httpRequestService, "secretKey", "titi");
    }

}
