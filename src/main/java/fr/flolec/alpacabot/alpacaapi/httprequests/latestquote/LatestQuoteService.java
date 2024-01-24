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

@Component
public class LatestQuoteService {

    @Value("${PAPER_LATEST_QUOTES_ENDPOINT}")
    private String endpoint;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpRequestService httpRequestService;

    public double getLatestQuote(AssetModel asset) throws IOException {
        return getLatestQuote(asset.getSymbol());
    }

    public double getLatestQuote(String symbol) throws IOException {
        String url = HttpUrl.parse(endpoint).newBuilder()
                .addQueryParameter("symbols", symbol)
                .toString();
        Response response = httpRequestService.get(url);
        JsonNode jsonNode = objectMapper.readTree(response.body().string()).path("quotes").path(symbol).path("ap");
        return objectMapper.treeToValue(jsonNode, Double.class);
    }

}
