package fr.flolec.alpacabot.alpacaapi.httprequests.position;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

@Component
public class PositionService {

    public static final String MESSAGE = "message";
    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;
    private final Logger logger = LoggerFactory.getLogger(PositionService.class);

    @Autowired
    public PositionService(@Value("${PAPER_POSITIONS_ENDPOINT}") String endpoint,
                           ObjectMapper objectMapper,
                           HttpRequestService httpRequestService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
    }

    public String takeSlashOutOfSymbol(String symbol) {
        if (symbol.contains("/")) {
            String[] symbols = symbol.split("/");
            return symbols[0].trim() + symbols[1].trim();
        }
        return symbol;
    }

    public PositionModel getAnOpenPosition(String symbol) throws IOException {
        symbol = takeSlashOutOfSymbol(symbol);
        Response response = httpRequestService.get(endpoint + "/" + symbol);
        assert response.body() != null;

        if (!response.isSuccessful()) {
            String errorMessage = objectMapper.readTree(response.body().string()).get(MESSAGE).asText();
            logger.warn("Position couldn't be retrieved: '{}' (code {})", errorMessage, response.code());
            return null;
        }

        String bodyString = response.body().string();
        JsonNode jsonNode = objectMapper.readTree(bodyString);
        return objectMapper.treeToValue(jsonNode, PositionModel.class);
    }

    public List<PositionModel> getAllOpenPositions() throws IOException {
        Response response = httpRequestService.get(endpoint);
        assert response.body() != null;
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        return objectMapper.treeToValue(jsonNode, new TypeReference<>() {
        });
    }

    public double getCurrentQtyOfAsset(String symbol) throws IOException {
        PositionModel assetCurrentPosition = getAnOpenPosition(symbol);
        if (assetCurrentPosition.getQuantity() == null) return 0;
        else return Double.parseDouble(assetCurrentPosition.getQuantity());
    }


    // Liquidate = sell order at market price
    public OrderModel liquidatePositionByPercentage(String symbol, double percentage) throws IOException {
        symbol = takeSlashOutOfSymbol(symbol);
        String url = requireNonNull(HttpUrl.parse(endpoint + "/" + symbol)).newBuilder()
                .addQueryParameter("percentage", String.format(Locale.US, "%.9f", percentage))
                .toString();
        return liquidatePosition(url);
    }

    public OrderModel liquidatePositionByQuantity(String symbol, double coinQuantity) throws IOException {
        symbol = takeSlashOutOfSymbol(symbol);
        String url = requireNonNull(HttpUrl.parse(endpoint + "/" + symbol)).newBuilder()
                .addQueryParameter("qty", String.format(Locale.US, "%.9f", coinQuantity))
                .toString();
        return liquidatePosition(url);
    }

    public OrderModel liquidatePosition(String url) throws IOException {
        Response response;
        JsonNode jsonNode;
        response = httpRequestService.delete(url);
        assert response.body() != null;

        if (!response.isSuccessful()) {
            String errorMessage = objectMapper.readTree(response.body().string()).get(MESSAGE).asText();
            logger.warn("Position couldn't be liquidated: '{}' (code {})", errorMessage, response.code());
            return null;
        }

        String responseString = response.body().string();
        jsonNode = objectMapper.readTree(responseString);
        return objectMapper.treeToValue(jsonNode, OrderModel.class);
    }

    public List<OrderModel> liquidateAllPositions() throws IOException {
        Response response;
        String url = requireNonNull(HttpUrl.parse(endpoint)).newBuilder()
                .addQueryParameter("cancel_orders", "true")
                .toString();
        response = httpRequestService.delete(url);
        assert response.body() != null;

        if (!response.isSuccessful()) {
            String errorMessage = objectMapper.readTree(response.body().string()).get(MESSAGE).asText();
            logger.warn("Positions couldn't be liquidated: '{}' (code {})", errorMessage, response.code());
            return new ArrayList<>();
        }
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        List<OrderModel> orderModels = new ArrayList<>();
        for (JsonNode node : jsonNode) {
            JsonNode bodyNode = node.get("body");
            OrderModel orderModel = objectMapper.treeToValue(bodyNode, OrderModel.class);
            orderModels.add(orderModel);
        }
        return orderModels;
    }

}


