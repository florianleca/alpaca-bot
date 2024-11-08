package fr.flolec.alpacabot.alpacaapi.httprequests.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Service
public class OrderService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Value("${ALPACA_API_ORDERS_URI}")
    private String uri;

    public OrderService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public OrderModel getOrderById(String orderId) {
        try {
            ResponseEntity<OrderModel> response = restClient.get()
                    .uri(uri + "/" + orderId)
                    .retrieve()
                    .toEntity(OrderModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.warn("Could not retrieve order of id '{}': {}", orderId, e.getMessage());
            return null;
        }
    }

    /**
     * @param symbol      The symbol, asset ID, or currency pair to identify the asset to trade
     * @param notional    The dollar amount to trade
     * @param side        Represents which side this order was on (buy or sell)
     * @param timeInForce For Crypto Trading, Alpaca supports the following Time-In-Force designations: day, gtc, ioc and fok
     * @return The order object resulting from the proposed transaction (might not be filled yet)
     */
    public OrderModel createMarketNotionalOrder(String symbol,
                                                String notional,
                                                OrderSide side,
                                                TimeInForce timeInForce) {
        PostOrderModel postOrder = new PostOrderModel();
        postOrder.setSymbol(symbol);
        postOrder.setNotional(notional);
        postOrder.setSide(side.toString());
        postOrder.setType("market");
        postOrder.setTimeInForce(timeInForce.toString());
        return createOrder(postOrder);
    }

    public OrderModel createLimitNotionalOrder(String symbol,
                                               String notional,
                                               OrderSide side,
                                               TimeInForce timeInForce,
                                               String limitPrice) {
        PostOrderModel postOrder = new PostOrderModel();
        postOrder.setSymbol(symbol);
        postOrder.setNotional(notional);
        postOrder.setSide(side.toString());
        postOrder.setType("limit");
        postOrder.setTimeInForce(timeInForce.toString());
        postOrder.setLimitPrice(limitPrice);
        return createOrder(postOrder);
    }

    public OrderModel createLimitQuantityOrder(String symbol,
                                               String quantity,
                                               OrderSide side,
                                               TimeInForce timeInForce,
                                               String limitPrice) {
        PostOrderModel postOrder = new PostOrderModel();
        postOrder.setSymbol(symbol);
        postOrder.setQuantity(quantity);
        postOrder.setSide(side.toString());
        postOrder.setType("limit");
        postOrder.setTimeInForce(timeInForce.toString());
        postOrder.setLimitPrice(limitPrice);
        return createOrder(postOrder);
    }

    private OrderModel createOrder(PostOrderModel postOrder) {
        try {
            String response = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(postOrder)
                    .retrieve()
                    .body(String.class);
            logger.info("An order has been created: {}", response);
            return objectMapper.readValue(response, OrderModel.class);
        } catch (HttpStatusCodeException | JsonProcessingException e) {
            logger.warn("Order could not be created: {}", e.getMessage());
            return null;
        }
    }

}
