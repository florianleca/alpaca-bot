package fr.flolec.alpacabot.alpacaapi.httprequests.order;

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
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;
    private final OrderRepository orderRepository;
    private final PositionService positionService;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    public OrderService(@Value("${PAPER_ORDERS_ENDPOINT}") String endpoint,
                        ObjectMapper objectMapper,
                        HttpRequestService httpRequestService,
                        OrderRepository orderRepository,
                        PositionService positionService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
        this.orderRepository = orderRepository;
        this.positionService = positionService;
    }

    public OrderModel getOrderById(String orderId) throws IOException {
        Response response = httpRequestService.get(endpoint + "/" + orderId);
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        return objectMapper.treeToValue(jsonNode, OrderModel.class);
    }

    /**
     * @param symbol      The symbol, asset ID, or currency pair to identify the asset to trade
     * @param notional    The dollar amount to trade
     * @param side        Represents which side this order was on (buy or sell)
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
        return createOrder(symbol, jsonBody);
    }

    public OrderModel createLimitNotionalOrder(String symbol, String notional, OrderSide side, TimeInForce timeInForce, String limitPrice) throws IOException {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put(OrderFieldsNames.SYMBOL.toString(), symbol)
                .put(OrderFieldsNames.NOTIONAL.toString(), notional)
                .put(OrderFieldsNames.SIDE.toString(), side.toString())
                .put(OrderFieldsNames.TYPE.toString(), "limit")
                .put(OrderFieldsNames.TIME_IN_FORCE.toString(), timeInForce.toString())
                .put(OrderFieldsNames.LIMIT_PRICE.toString(), limitPrice);
        return createOrder(symbol, jsonBody);
    }

    public OrderModel createLimitQuantityOrder(String symbol, String quantity, OrderSide side, TimeInForce timeInForce, String limitPrice) throws IOException {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put(OrderFieldsNames.SYMBOL.toString(), symbol)
                .put(OrderFieldsNames.QUANTITY.toString(), quantity)
                .put(OrderFieldsNames.SIDE.toString(), side.toString())
                .put(OrderFieldsNames.TYPE.toString(), "limit")
                .put(OrderFieldsNames.TIME_IN_FORCE.toString(), timeInForce.toString())
                .put(OrderFieldsNames.LIMIT_PRICE.toString(), limitPrice);
        return createOrder(symbol, jsonBody);
    }

    @NotNull
    private OrderModel createOrder(String symbol, ObjectNode jsonBody) throws IOException {
        // Récupérer la quantité de cet asset possédée avant l'ordre pour plus tard set l'attribut de l'ordre
//        double qtyBeforeOrder;
//        try {
//            qtyBeforeOrder = Double.parseDouble(positionService.getAnOpenPosition(symbol).getQuantity());
//        } catch (NullPointerException e) {
//            qtyBeforeOrder = 0;
//        }
        // Réaliser l'ordre
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Response response = httpRequestService.post(endpoint, body);
        String responseString = response.body().string();
        logger.info("Response after ordering: {}", responseString);
        JsonNode jsonNode = objectMapper.readTree(responseString);
        OrderModel createdOrder = objectMapper.treeToValue(jsonNode, OrderModel.class);
//        createdOrder.setPositionQtyBeforeOrder(qtyBeforeOrder);
        return createdOrder;
    }

    public void cancelAllOrders() throws IOException {
        httpRequestService.delete(endpoint);
    }

    public void archive(OrderModel order) {
        orderRepository.save(order);
    }

    public void fillOrder(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message).path("data").path("order");
            OrderModel filledOrder = objectMapper.treeToValue(jsonNode, OrderModel.class);
            fillOrder(filledOrder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fillOrder(OrderModel filledOrder) {
        orderRepository.deleteById(filledOrder.getId());
        archive(filledOrder);
    }

//        PositionModel assetCurrentPosition = positionService.getAnOpenPosition(buyOrder.getSymbol());
//        double positionQtyAfterOrder = Double.parseDouble(assetCurrentPosition.getQuantity());
//        double quantityToSell = positionQtyAfterOrder - buyOrder.getPositionQtyBeforeOrder();
//        OrderModel sellOrder = createLimitQuantityOrder(
//                buyOrder.getSymbol(),
//                String.valueOf(quantityToSell),
//                OrderSide.SELL,
//                TimeInForce.GTC,
//                String.valueOf(buyOrder.getFilledAvgPrice() * (1 + (gainPercentage / 100 ))));

    public void updateUnfilledOrders() {
        logger.info("Updating potential filled orders...");
        List<OrderModel> unfilledOrders = orderRepository.findUnfilledOrders();     // On récupère les ordres qui ne sont pas 'filled' en BD,
        unfilledOrders.forEach(order -> {                                           // et pour chacun d'entre eux...
            try {
                OrderModel potentiallyFilledOrder = getOrderById(order.getId());    // ... on récupère la version potentiellement filled via l'API.
                if (potentiallyFilledOrder.getFilledAt() != null) {                 // Si l'ordre a été filled,
                    fillOrder(potentiallyFilledOrder);                              // on actualise la version en BD.
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        logger.info("Update done!");
    }

    public long countUnfilledBuyOrder(String symbol) {
        return orderRepository.countUnfilledBuyOrder(symbol);
    }

    /**
     * @param symbol The symbol of the asset
     * @return The list of unsold buy orders of this asset, meaning filled buy orders with unfilled dual sell orders
     */
    public List<OrderModel> getUnsoldBuyOrders(String symbol) {
        List<OrderModel> unsoldBuyOrders = new ArrayList<>();
        List<OrderModel> allFilledBuyOrders = orderRepository.findFilledBuyOrders(symbol).stream().toList();    // On récupère tous les ordres d'achat filled
        allFilledBuyOrders.forEach(order -> {                                                                   // Pour chacun d'entre eux...
            OrderModel dualSellOrder = orderRepository.findById(order.getDualOrderId()).orElse(null);
            assert dualSellOrder != null;
            if (dualSellOrder.getFilledAt() == null) {                                                          // Si son dual est unfilled,
                unsoldBuyOrders.add(order);                                                                     // on l'ajoute.
            }
        });
        return unsoldBuyOrders;
    }
}
