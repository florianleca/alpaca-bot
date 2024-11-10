package fr.flolec.alpacabot.alpacaapi.httprequests;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Value("${ALPACA_API_KEY_ID}")
    private String keyId;

    @Value("${ALPACA_API_SECRET_KEY}")
    private String secretKey;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    // Possibilité de rajouter un intercepteur (ClientHttpRequestInterceptor) pour logger les requêtes
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestFactory(new JdkClientHttpRequestFactory())
                .defaultHeader("APCA-API-KEY-ID", keyId)
                .defaultHeader("APCA-API-SECRET-KEY", secretKey);
    }

}
