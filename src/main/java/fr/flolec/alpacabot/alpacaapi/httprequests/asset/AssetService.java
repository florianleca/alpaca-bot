package fr.flolec.alpacabot.alpacaapi.httprequests.asset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
    public List<AssetModel> getAssetsList() {
        ResponseEntity<List<AssetModel>> response = restClient.get()
                .uri(UriComponentsBuilder
                        .fromUriString(uri)
                        .queryParam("status", "active")
                        .queryParam("exchange", "CRYPTO")
                        .toUriString())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        List<AssetModel> assets = response.getBody();
        if (assets == null) return List.of();
        selectUSDAssets(assets);
        return assets.stream().sorted(Comparator.comparing(AssetModel::getName)).toList();
    }

    public void selectUSDAssets(List<AssetModel> assets) {
        assets.removeIf(asset -> !asset.getName().contains(" / US Dollar"));
    }

}
