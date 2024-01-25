package fr.flolec.alpacabot.alpacaapi.httprequests.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class OrderService {

    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;
    private final OrderRepository orderRepository;
    private final PositionService positionService;
    private final double gainPercentage;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    public OrderService(@Value("${PAPER_ORDERS_ENDPOINT}") String endpoint,
                        @Value("${GAIN_PERCENTAGE}") double gainPercentage,
                        ObjectMapper objectMapper,
                        HttpRequestService httpRequestService,
                        OrderRepository orderRepository,
                        PositionService positionService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
        this.orderRepository = orderRepository;
        this.positionService = positionService;
        this.gainPercentage = gainPercentage;
    }

    /**
     *
     * @param symbol The symbol, asset ID, or currency pair to identify the asset to trade
     * @param notional The dollar amount to trade
     * @param side Represents which side this order was on (buy or sell)
     * @param timeInForce For Crypto Trading, Alpaca supports the following Time-In-Force designations: day, gtc, ioc and fok
     * @return The order object resulting from the proposed transaction (might not be filled yet)
     * @throws IOException If an I/O error occurs while fetching or processing the data
     */
    public OrderModel createMarketNotionalOrder(String symbol, String notional, OrderSide side, TimeInForce timeInForce) throws IOException {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put(OrderFieldsNames.SYMBOL.toString(), symbol)
                .put(OrderFieldsNames.NOTIONAL.toString(), notional)
                .put(OrderFieldsNames.SIDE.toString(), side.toString())
                .put(OrderFieldsNames.TYPE.toString(), "market")
                .put(OrderFieldsNames.TIME_IN_FORCE.toString(), timeInForce.toString());
        return sendOrder(symbol, jsonBody);
    }

    public OrderModel createLimitNotionalOrder(String symbol, String notional, OrderSide side, TimeInForce timeInForce, String limitPrice) throws IOException {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put(OrderFieldsNames.SYMBOL.toString(), symbol)
                .put(OrderFieldsNames.NOTIONAL.toString(), notional)
                .put(OrderFieldsNames.SIDE.toString(), side.toString())
                .put(OrderFieldsNames.TYPE.toString(), "limit")
                .put(OrderFieldsNames.TIME_IN_FORCE.toString(), timeInForce.toString())
                .put(OrderFieldsNames.LIMIT_PRICE.toString(), limitPrice);
        return sendOrder(symbol, jsonBody);
    }

    public OrderModel createLimitQuantityOrder(String symbol, String quantity, OrderSide side, TimeInForce timeInForce, String limitPrice) throws IOException {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put(OrderFieldsNames.SYMBOL.toString(), symbol)
                .put(OrderFieldsNames.QUANTITY.toString(), quantity)
                .put(OrderFieldsNames.SIDE.toString(), side.toString())
                .put(OrderFieldsNames.TYPE.toString(), "limit")
                .put(OrderFieldsNames.TIME_IN_FORCE.toString(), timeInForce.toString())
                .put(OrderFieldsNames.LIMIT_PRICE.toString(), limitPrice);
        return sendOrder(symbol, jsonBody);
    }

    @NotNull
    private OrderModel sendOrder(String symbol, ObjectNode jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Response response = httpRequestService.post(endpoint, body);
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        OrderModel createdOrder = objectMapper.treeToValue(jsonNode, OrderModel.class);
        try {
            createdOrder.setPositionQtyBeforeOrder(Double.parseDouble(positionService.getAnOpenPosition(symbol).getQuantity()));
        } catch (NullPointerException e) {
            createdOrder.setPositionQtyBeforeOrder(0);
        }

        return createdOrder;
    }

    public OrderModel getOrderById(String orderId) throws IOException {
        Response response = httpRequestService.get(endpoint + "/" + orderId);
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        return objectMapper.treeToValue(jsonNode, OrderModel.class);
    }

    public void archive(OrderModel order) {
        orderRepository.save(order);
    }

    public void replaceWithFilledVersion(OrderModel buyOrder, OrderModel sellOrder) {
        orderRepository.deleteById(buyOrder.getId());   // On récupère l'ancien ordre d'achat
        buyOrder.setDualOrderId(sellOrder.getId());     // On lui affecte l'ordre de vente comme ordre dual
        sellOrder.setDualOrderId(buyOrder.getId());     // Et inversement
        orderRepository.save(buyOrder);                 // On sauvegarde la version FILLED de l'ordre d'achat
        orderRepository.save(sellOrder);                // On sauvegarde la version UNFILLED de l'ordre de vente
    }

    public void replaceSellOrderWithFilledVersion(OrderModel filledSellOrder) {
        OrderModel unfilledSellOrder = orderRepository.findById(filledSellOrder.getId()).orElse(null);
        orderRepository.deleteById(filledSellOrder.getId());
        try {
            filledSellOrder.setDualOrderId(unfilledSellOrder.getDualOrderId());
        } catch (NullPointerException e) {
            // TODO
        }

        orderRepository.save(filledSellOrder);
    }

    public List<OrderModel> getUnsoldOrders(String assetId) {
        return orderRepository.findUnsoldOrders(assetId);
    }

    public void fillOrder(String message) {
        if (message.contains("\"side\":\"buy\"")) {
            fillBuyOrder(message);
        } else if (message.contains("\"side\":\"sell\"")) {
            fillSellOrder(message);
        }
    }

    private void fillBuyOrder(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message).path("data").path("order");
            OrderModel buyOrder = objectMapper.treeToValue(jsonNode, OrderModel.class);
            fillBuyOrder(buyOrder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillBuyOrder(OrderModel buyOrder) {
        try {
            double positionQtyAfterOrder = Double.parseDouble(positionService.getAnOpenPosition(buyOrder.getSymbol()).getQuantity());
            double quantityToSell = positionQtyAfterOrder - buyOrder.getPositionQtyBeforeOrder();
            OrderModel sellOrder = createLimitQuantityOrder(
                    buyOrder.getSymbol(),
                    String.valueOf(quantityToSell),
                    OrderSide.SELL,
                    TimeInForce.GTC,
                    String.valueOf(buyOrder.getFilledAvgPrice() * (1 + (gainPercentage / 100 ))));
            replaceWithFilledVersion(buyOrder, sellOrder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillSellOrder(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message).path("data").path("order");
            OrderModel sellOrder = objectMapper.treeToValue(jsonNode, OrderModel.class);
            replaceSellOrderWithFilledVersion(sellOrder);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUnfilledOrders() {
        logger.info("Updating potential filled orders...");
        // On récupère la liste des ordres qui ne sont pas 'filled' dans la BD
        List<OrderModel> unfilledOrders = orderRepository.findUnfilledOrders();
        // Et pour chacun d'entre eux...
        unfilledOrders.forEach(order -> {
            try {
                // ... on récupère la version potentiellement filled via l'API Alpaca
                OrderModel newOrder = getOrderById(order.getId());
                if (newOrder.getSide() == null) {
                    // TODO
                }
                // Si c'est un ordre d'achat qui a été filled, on 'fillBuyOrder',
                // ce qui va mettre à jour l'ordre d'achat en BD + créer un ordre de vente
                else if (newOrder.getSide().equals("buy") && newOrder.getFilledAt() != null) {
                    fillBuyOrder(newOrder);
                // Si c'est un ordre de vente qui a été filled, on se contente d'actualiser la version en BD
                } else if (newOrder.getSide().equals("sell") && newOrder.getFilledAt() != null) {
                    replaceSellOrderWithFilledVersion(newOrder);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        logger.info("Update done!");
    }
}
