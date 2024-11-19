package fr.flolec.alpacabot.alpacaapi.bar.latestbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.bar.BarModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class LatestBarService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    @Value("${ALPACA_DATA_LATEST_BARS_URI_CRYPTO}")
    private String uriCrypto;
    @Value("${ALPACA_DATA_LATEST_BARS_URI_STOCKS}")
    private String uriStocks;

    public LatestBarService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public double getLatestBarCloseValue(AssetModel asset, boolean isCrypto) throws AlpacaApiException, JsonProcessingException {
        return getLatestBarCloseValue(asset.getSymbol(), isCrypto);
    }

    public double getLatestBarCloseValue(String symbol, boolean isCrypto) throws AlpacaApiException, JsonProcessingException {
        String uri = isCrypto ? uriCrypto : uriStocks;
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri)
                            .queryParam("symbols", symbol)
                            .toUriString())
                    .retrieve()
                    .toEntity(String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody()).path("bars").path(symbol);
            BarModel barModel = objectMapper.treeToValue(jsonNode, BarModel.class);
            return barModel.getClose();
        } catch (HttpStatusCodeException e) {
            String message = String.format("Latest bar of %s could not be retrieved", symbol);
            throw new AlpacaApiException(e, message);
        }
    }

}
