package fr.flolec.alpacabot.alpacaapi.httprequests.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OrderService {

    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    public OrderService(@Value("${PAPER_ORDERS_ENDPOINT}") String endpoint,
                        ObjectMapper objectMapper,
                        HttpRequestService httpRequestService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
    }

    public OrderModel getOrderById(String orderId) throws IOException {
        Response response = httpRequestService.get(endpoint + "/" + orderId);
        assert response.body() != null;

        if (!response.isSuccessful()) {
            String errorMessage = objectMapper.readTree(response.body().string()).get("message").asText();
            logger.warn("Order of id '{}' not found: '{}' (code {})", orderId, errorMessage, response.code());
            return null;
        }

        String jsonString = response.body().string();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        return objectMapper.treeToValue(jsonNode, OrderModel.class);
    }

    /**
     * @param symbol      The symbol, asset ID, or currency pair to identify the asset to trade
     * @param notional    The dollar amount to trade
     * @param side        Represents which side this order was on (buy or sell)
     * @param timeInForce For Crypto Trading, Alpaca supports the following Time-In-Force designations: day, gtc, ioc and fok
     * @return The order object resulting from the proposed transaction (might not be filled yet)
     */
    public OrderModel createMarketNotionalOrder(String symbol, String notional, OrderSide side, TimeInForce timeInForce) throws IOException {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put(OrderFieldsNames.SYMBOL.toString(), symbol)
                .put(OrderFieldsNames.NOTIONAL.toString(), notional)
                .put(OrderFieldsNames.SIDE.toString(), side.toString())
                .put(OrderFieldsNames.TYPE.toString(), "market")
                .put(OrderFieldsNames.TIME_IN_FORCE.toString(), timeInForce.toString());
        return createOrder(jsonBody);
    }

    public OrderModel createLimitNotionalOrder(String symbol, String notional, OrderSide side, TimeInForce timeInForce, String limitPrice) throws IOException {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put(OrderFieldsNames.SYMBOL.toString(), symbol)
                .put(OrderFieldsNames.NOTIONAL.toString(), notional)
                .put(OrderFieldsNames.SIDE.toString(), side.toString())
                .put(OrderFieldsNames.TYPE.toString(), "limit")
                .put(OrderFieldsNames.TIME_IN_FORCE.toString(), timeInForce.toString())
                .put(OrderFieldsNames.LIMIT_PRICE.toString(), limitPrice);
        return createOrder(jsonBody);
    }

    public OrderModel createLimitQuantityOrder(String symbol, String quantity, OrderSide side, TimeInForce timeInForce, String limitPrice) throws IOException {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put(OrderFieldsNames.SYMBOL.toString(), symbol)
                .put(OrderFieldsNames.QUANTITY.toString(), quantity)
                .put(OrderFieldsNames.SIDE.toString(), side.toString())
                .put(OrderFieldsNames.TYPE.toString(), "limit")
                .put(OrderFieldsNames.TIME_IN_FORCE.toString(), timeInForce.toString())
                .put(OrderFieldsNames.LIMIT_PRICE.toString(), limitPrice);
        return createOrder(jsonBody);
    }

    private OrderModel createOrder(ObjectNode jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Response response = httpRequestService.post(endpoint, body);
        assert response.body() != null;

        if (!response.isSuccessful()) {
            String errorMessage = objectMapper.readTree(response.body().string()).get("message").asText();
            logger.warn("Order was not created: '{}' (code {})", errorMessage, response.code());
            return null;
        }

        String responseString = response.body().string();
        logger.info("Response after ordering: {}", responseString);
        JsonNode jsonNode = objectMapper.readTree(responseString);
        return objectMapper.treeToValue(jsonNode, OrderModel.class);
    }

    public OrderModel messageToOrder(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message).path("data").path("order");
            return objectMapper.treeToValue(jsonNode, OrderModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
