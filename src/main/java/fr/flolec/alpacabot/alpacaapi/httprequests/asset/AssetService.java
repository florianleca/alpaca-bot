package fr.flolec.alpacabot.alpacaapi.httprequests.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;

@Component
public class AssetService {

    @Value("${ALPACA_API_ASSETS_URI}")
    private String uri;

    private final RestClient restClient;

    private final Logger logger = LoggerFactory.getLogger(AssetService.class);

    public AssetService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @return Sorted list of all active crypto assets in USD
     */
    public List<AssetModel> getAssetsList(AssetClass assetClass) {
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
            if (assets != null) {
                selectUSDAssets(assets);
                selectTradableAssets(assets);
                return assets.stream().sorted(Comparator.comparing(AssetModel::getName)).toList();
            }
        } catch (HttpStatusCodeException e) {
            logger.warn("Assets list could not be retrieved: {}", e.getMessage());
        }
        return List.of();
    }

    public void selectUSDAssets(List<AssetModel> assets) {
        assets.removeIf(asset -> "CRYPTO".equals(asset.getExchange()) && !asset.getName().contains(" / US Dollar"));
    }

    public void selectTradableAssets(List<AssetModel> assets) {
        assets.removeIf(asset -> !asset.getTradable());
    }

}
