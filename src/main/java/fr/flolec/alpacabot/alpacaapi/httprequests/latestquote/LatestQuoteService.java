package fr.flolec.alpacabot.alpacaapi.httprequests.latestquote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class LatestQuoteService {

    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;

    @Autowired
    public LatestQuoteService(@Value("${PAPER_LATEST_QUOTES_ENDPOINT}") String endpoint,
                              ObjectMapper objectMapper,
                              HttpRequestService httpRequestService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
    }

    public double getLatestQuote(AssetModel asset) throws IOException {
        return getLatestQuote(asset.getSymbol());
    }

    public double getLatestQuote(String symbol) throws IOException {
        String url = Objects.requireNonNull(HttpUrl.parse(endpoint)).newBuilder()
                .addQueryParameter("symbols", symbol)
                .toString();
        Response response = httpRequestService.get(url);
        assert response.body() != null;
        JsonNode jsonNode = objectMapper.readTree(response.body().string()).path("quotes").path(symbol).path("ap");
        return objectMapper.treeToValue(jsonNode, Double.class);
    }

}
