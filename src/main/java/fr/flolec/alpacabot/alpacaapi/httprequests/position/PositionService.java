package fr.flolec.alpacabot.alpacaapi.httprequests.position;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(PositionService.class);

    @Value("${ALPACA_API_POSITIONS_URI}")
    private String uri;

    public PositionService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String takeSlashOutOfSymbol(String symbol) {
        if (symbol.contains("/")) {
            String[] symbols = symbol.split("/");
            return symbols[0].trim() + symbols[1].trim();
        }
        return symbol;
    }

    public PositionModel getAnOpenPosition(String symbol) {
        try {
            symbol = takeSlashOutOfSymbol(symbol);
            ResponseEntity<PositionModel> response = restClient.get()
                    .uri(uri + "/" + symbol)
                    .retrieve()
                    .toEntity(PositionModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.warn("{} position could not be retrieved: {}", symbol, e.getMessage());
            return null;
        }
    }

    public List<PositionModel> getAllOpenPositions() {
        try {
            ResponseEntity<List<PositionModel>> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.warn("All open positions could not be retrieved: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public double getCurrentQtyOfAsset(String symbol) {
        PositionModel assetCurrentPosition = getAnOpenPosition(symbol);
        if (assetCurrentPosition == null || assetCurrentPosition.getQuantity() == null) return 0;
        else return Double.parseDouble(assetCurrentPosition.getQuantity());
    }

    // Liquidate = sell order at market price
    public OrderModel liquidatePositionByPercentage(String symbol, double percentage) {
        try {
            symbol = takeSlashOutOfSymbol(symbol);
            ResponseEntity<OrderModel> response = restClient.delete()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri + "/" + symbol)
                            .queryParam("percentage", String.format(Locale.US, "%.9f", percentage))
                            .toUriString())
                    .retrieve()
                    .toEntity(OrderModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.warn("{} position could not be liquidated by percentage: {}", symbol, e.getMessage());
            return null;
        }
    }

    public OrderModel liquidatePositionByQuantity(String symbol, double coinQuantity) {
        try {
            symbol = takeSlashOutOfSymbol(symbol);
            ResponseEntity<OrderModel> response = restClient.delete()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri + "/" + symbol)
                            .queryParam("qty", String.format(Locale.US, "%.9f", coinQuantity))
                            .toUriString())
                    .retrieve()
                    .toEntity(OrderModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.warn("{} position could not be liquidated by quantity: {}", symbol, e.getMessage());
            return null;
        }
    }

    public OrderModel liquidatePosition(String symbol) {
        try {
            symbol = takeSlashOutOfSymbol(symbol);
            ResponseEntity<OrderModel> response = restClient.delete()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri + "/" + symbol)
                            .toUriString())
                    .retrieve()
                    .toEntity(OrderModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.warn("{} position could not be liquidated: {}", symbol, e.getMessage());
            return null;
        }
    }

    public List<OrderModel> liquidateAllPositions(boolean cancelOrders) {
        try {
            ResponseEntity<List<MultipleOrders>> response = restClient.delete()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri)
                            .queryParam("cancel_orders", cancelOrders)
                            .toUriString())
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });
            assert response.getBody() != null;
            return response.getBody().stream().map(MultipleOrders::getOrder).toList();
        } catch (HttpStatusCodeException e) {
            logger.warn("All positions could not be liquidated: {}", e.getMessage());
            return new ArrayList<>();
        }

    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MultipleOrders {

        @JsonProperty("body")
        private OrderModel order;

    }

}


