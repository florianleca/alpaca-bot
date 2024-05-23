package fr.flolec.alpacabot.alpacaapi.httprequests.asset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class AssetService {

    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;

    @Autowired
    public AssetService(@Value("${PAPER_ASSETS_ENDPOINT}") String endpoint,
                        ObjectMapper objectMapper,
                        HttpRequestService httpRequestService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
    }

    /**
     * @return Sorted list of all active crypto assets in USD
     * @throws IOException If an I/O error occurs while fetching or processing the data
     */
    public List<AssetModel> getAssetsList() throws IOException {
        String url = Objects.requireNonNull(HttpUrl.parse(endpoint)).newBuilder()
                .addQueryParameter("status", "active")
                .addQueryParameter("exchange", "CRYPTO")
                .toString();
        Response response = httpRequestService.get(url);
        assert response.body() != null;
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        List<AssetModel> assets = objectMapper.treeToValue(jsonNode, new TypeReference<ArrayList<AssetModel>>() {
        });
        selectUSDAssets(assets);
        Collections.sort(assets);
        return assets;
    }

    public void selectUSDAssets(List<AssetModel> assets) {
        assets.removeIf(asset -> !asset.getName().contains(" / US Dollar"));
    }

}
