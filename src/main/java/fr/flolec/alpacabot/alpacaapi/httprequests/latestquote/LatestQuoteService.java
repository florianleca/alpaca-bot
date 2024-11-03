package fr.flolec.alpacabot.alpacaapi.httprequests.latestquote;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class LatestQuoteService {

    private final RestClient restClient;

    @Value("${ALPACA_DATA_LATEST_QUOTES_URI}")
    private String uri;

    public LatestQuoteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public double getLatestQuote(AssetModel asset) {
        return getLatestQuote(asset.getSymbol());
    }

    public double getLatestQuote(String symbol) {
        ResponseEntity<LatestQuoteModel> response = restClient.get()
                .uri(UriComponentsBuilder
                        .fromUriString(uri)
                        .queryParam("symbols", symbol)
                        .toUriString())
                .retrieve()
                .toEntity(LatestQuoteModel.class);
        return response.getBody().getQuotes().get(symbol).getAskPrice();
    }

}
