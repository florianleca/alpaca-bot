package fr.flolec.alpacabot.alpacaapi.httprequests.latestquote;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class LatestQuoteService {

    private final RestClient restClient;
    private final Logger logger = LoggerFactory.getLogger(LatestQuoteService.class);

    @Value("${ALPACA_DATA_LATEST_QUOTES_URI}")
    private String uri;

    public LatestQuoteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public double getLatestQuote(AssetModel asset) {
        return getLatestQuote(asset.getSymbol());
    }

    public double getLatestQuote(String symbol) {
        try {
            ResponseEntity<LatestQuoteModel> response = restClient.get()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri)
                            .queryParam("symbols", symbol)
                            .toUriString())
                    .retrieve()
                    .toEntity(LatestQuoteModel.class);
            LatestQuoteModel latestQuoteModel = response.getBody();
            if (latestQuoteModel != null) {
                return latestQuoteModel.getQuotes().get(symbol).getAskPrice();
            }
        } catch (HttpStatusCodeException e) {
            logger.warn("Latest quote of {} could not be retrieved: {}", symbol, e.getMessage());
        }
        return 0;
    }

}
