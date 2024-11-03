package fr.flolec.alpacabot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.RestClientConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@WireMockTest(httpPort = 8080)
public abstract class WireMockedTest {

    protected final ObjectMapper objectMapper;
    protected final RestClientConfiguration restClientConfiguration;

    public WireMockedTest() {
        objectMapper = new ObjectMapper();
        restClientConfiguration = new RestClientConfiguration();// Utilisation réelle du service HTTP
        ReflectionTestUtils.setField(restClientConfiguration, "keyId", "toto");
        ReflectionTestUtils.setField(restClientConfiguration, "secretKey", "titi");
    }

}
