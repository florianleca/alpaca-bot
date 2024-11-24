package fr.flolec.alpacabot.alpacaapi.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.AlpacaWebSocketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AssetService {

    private final AlpacaWebSocketService alpacaWebSocketService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    @Value("${ALPACA_API_ASSETS_URI}")
    private String uri;

    public AssetService(RestClient restClient,
                        ObjectMapper objectMapper,
                        AlpacaWebSocketService alpacaWebSocketService) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.alpacaWebSocketService = alpacaWebSocketService;
    }

    public List<AssetModel> getAssetsList(AssetClass assetClass) throws AlpacaApiException {
        try {
            ResponseEntity<List<AssetModel>> response = restClient.get()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri)
                            .queryParam("status", "active")
                            .queryParam("asset_class", assetClass.getLabel())
                            .toUriString())
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });
            List<AssetModel> assets = response.getBody();

            if (assets == null) return List.of();

            if (AssetClass.CRYPTO.equals(assetClass)) selectUSDAssets(assets);
            selectTradableAssets(assets);
            return assets.stream().sorted(Comparator.comparing(AssetModel::getName)).toList();
        } catch (HttpStatusCodeException e) {
            throw new AlpacaApiException(e, "Assets list could not be retrieved");
        }
    }

    public void selectUSDAssets(List<AssetModel> assets) {
        assets.removeIf(asset -> !asset.getName().contains(" / US Dollar"));
    }

    public void selectTradableAssets(List<AssetModel> assets) {
        assets.removeIf(asset -> !asset.getTradable());
    }

    public List<String> subscribeToCryptoAssets() throws AlpacaApiException, IOException {
        List<AssetModel> assets = getAssetsList(AssetClass.CRYPTO);
        List<String> assetsSymbols = assets.stream()
                .map(AssetModel::getSymbol)
                .toList();
        Map<String, Object> subscribeMap = new HashMap<>();
        subscribeMap.put("action", "subscribe");
        subscribeMap.put("bars", assetsSymbols);
        String subscribeMessage = objectMapper.writeValueAsString(subscribeMap);
        alpacaWebSocketService.sendMessage(subscribeMessage);
        return assetsSymbols;
    }

}
