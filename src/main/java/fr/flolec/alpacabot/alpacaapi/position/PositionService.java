package fr.flolec.alpacabot.alpacaapi.position;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.order.OrderModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class PositionService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    @Value("${ALPACA_API_POSITIONS_URI}")
    private String uri;

    public PositionService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public String takeSlashOutOfSymbol(String symbol) {
        if (symbol.contains("/")) {
            String[] symbols = symbol.split("/");
            return symbols[0].trim() + symbols[1].trim();
        }
        return symbol;
    }

    public PositionModel getAnOpenPosition(String symbol) throws AlpacaApiException {
        String cleanedSymbol = takeSlashOutOfSymbol(symbol);
        try {
            ResponseEntity<PositionModel> response = restClient.get()
                    .uri(uri + "/" + cleanedSymbol)
                    .retrieve()
                    .toEntity(PositionModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            String message = String.format("%s position could not be retrieved", symbol);
            throw new AlpacaApiException(e, message);
        }
    }

    public List<PositionModel> getAllOpenPositions() throws AlpacaApiException {
        try {
            ResponseEntity<List<PositionModel>> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new AlpacaApiException(e, "All open positions could not be retrieved");
        }
    }

    public double getCurrentQtyOfAsset(String symbol) throws AlpacaApiException {
        PositionModel assetCurrentPosition = getAnOpenPosition(symbol);
        if (assetCurrentPosition == null || assetCurrentPosition.getQuantity() == null) return 0;
        else return Double.parseDouble(assetCurrentPosition.getQuantity());
    }

    // Liquidate = sell order at market price
    public OrderModel liquidatePositionByPercentage(String symbol, double percentage) throws AlpacaApiException {
        String cleanedSymbol = takeSlashOutOfSymbol(symbol);
        try {
            ResponseEntity<OrderModel> response = restClient.delete()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri + "/" + cleanedSymbol)
                            .queryParam("percentage", String.format(Locale.US, "%.9f", percentage))
                            .toUriString())
                    .retrieve()
                    .toEntity(OrderModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            String message = String.format("%s position could not be liquidated by percentage", symbol);
            throw new AlpacaApiException(e, message);
        }
    }

    public OrderModel liquidatePositionByQuantity(String symbol, double coinQuantity) throws AlpacaApiException {
        String cleanedSymbol = takeSlashOutOfSymbol(symbol);
        try {
            ResponseEntity<OrderModel> response = restClient.delete()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri + "/" + cleanedSymbol)
                            .queryParam("qty", String.format(Locale.US, "%.9f", coinQuantity))
                            .toUriString())
                    .retrieve()
                    .toEntity(OrderModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            String message = String.format("%s position could not be liquidated by quantity", symbol);
            throw new AlpacaApiException(e, message);
        }
    }

    public OrderModel liquidatePosition(String symbol) throws AlpacaApiException {
        String cleanedSymbol = takeSlashOutOfSymbol(symbol);
        try {
            ResponseEntity<OrderModel> response = restClient.delete()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri + "/" + cleanedSymbol)
                            .toUriString())
                    .retrieve()
                    .toEntity(OrderModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            String message = String.format("%s position could not be liquidated", symbol);
            throw new AlpacaApiException(e, message);
        }
    }

    public List<OrderModel> liquidateAllPositions(boolean cancelOrders) throws AlpacaApiException, JsonProcessingException {
        List<OrderModel> orders = new ArrayList<>();
        JsonNode rootNode;
        try {
            ResponseEntity<String> response = restClient.delete()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri)
                            .queryParam("cancel_orders", cancelOrders)
                            .toUriString())
                    .retrieve()
                    .toEntity(String.class);
            rootNode = objectMapper.readTree(response.getBody());
        } catch (HttpStatusCodeException e) {
            throw new AlpacaApiException(e, "All positions could not be liquidated");
        }
        for (JsonNode node : rootNode) {
            JsonNode bodyNode = node.get("body");
            orders.add(objectMapper.treeToValue(bodyNode, OrderModel.class));
        }
        return orders;
    }

}


