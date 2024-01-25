package fr.flolec.alpacabot.alpacaapi.httprequests.position;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class PositionService {

    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;

    @Autowired
    public PositionService(@Value("${PAPER_POSITIONS_ENDPOINT}") String endpoint,
                           ObjectMapper objectMapper,
                           HttpRequestService httpRequestService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
    }

    // Liquidate = sell order at market price
    public OrderModel liquidatePositionByPercentage(String symbol, double percentage) {
        symbol = takeSlashOutOfSymbol(symbol);
        String url = HttpUrl.parse(endpoint + "/" + symbol).newBuilder()
                .addQueryParameter("percentage", String.format(Locale.US, "%.9f", percentage))
                .toString();
        return liquidatePosition(url);
    }

    public OrderModel liquidatePositionByQuantity(String symbol, double coinQuantity) {
        symbol = takeSlashOutOfSymbol(symbol);
        String url = HttpUrl.parse(endpoint + "/" + symbol).newBuilder()
                .addQueryParameter("qty", String.format(Locale.US, "%.9f", coinQuantity))
                .toString();
        return liquidatePosition(url);
    }

    public OrderModel liquidatePosition(String url) {
        Response response;
        JsonNode jsonNode;
        try {
            response = httpRequestService.delete(url);
            jsonNode = objectMapper.readTree(response.body().string());
            return objectMapper.treeToValue(jsonNode, OrderModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String takeSlashOutOfSymbol(String symbol) {
        String[] symbols = symbol.split("/");
        return symbols[0].trim() + symbols[1].trim();
    }

    public List<PositionModel> getAllOpenPositions() throws IOException {
        Response response = httpRequestService.get(endpoint);
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        return objectMapper.treeToValue(jsonNode, new TypeReference<ArrayList<PositionModel>>() {
        });
    }


    public PositionModel getAnOpenPosition(String symbol) throws IOException {
        symbol = takeSlashOutOfSymbol(symbol);
        Response response = httpRequestService.get(endpoint + "/" + symbol);
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        return objectMapper.treeToValue(jsonNode, PositionModel.class);
    }

}


