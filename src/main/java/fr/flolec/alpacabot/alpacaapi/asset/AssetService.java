package fr.flolec.alpacabot.alpacaapi.asset;

import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
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

    private final RestClient restClient;
    @Value("${ALPACA_API_ASSETS_URI}")
    private String uri;

    public AssetService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @return Sorted list of all active crypto assets in USD
     */
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

}
