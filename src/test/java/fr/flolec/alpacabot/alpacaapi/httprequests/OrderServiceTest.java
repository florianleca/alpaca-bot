package fr.flolec.alpacabot.alpacaapi.httprequests;

import fr.flolec.alpacabot.WireMockedTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest extends WireMockedTest {

    public static final String RESPONSE_MARKET_NATIONAL_ORDER = "{\"id\":\"5ebe667b-4694-4259-aabe-2eb96a4acdd1\",\"client_order_id\":\"07303f9c-a13c-4f9b-8f82-902662fdab9b\",\"created_at\":\"2024-06-03T16:25:51.118965912Z\",\"updated_at\":\"2024-06-03T16:25:51.120503952Z\",\"submitted_at\":\"2024-06-03T16:25:51.118965912Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":\"1.27\",\"qty\":null,\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"market\",\"type\":\"market\",\"side\":\"buy\",\"time_in_force\":\"gtc\",\"limit_price\":null,\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null}\n";
    public static final String RESPONSE_LIMIT_NATIONAL_ORDER = "{\"id\":\"2cf1dcb8-c814-43ca-848d-cc2aec713a8c\",\"client_order_id\":\"2f89521f-b19e-48b8-9b3c-8e58573840f0\",\"created_at\":\"2024-06-03T16:30:00.338722766Z\",\"updated_at\":\"2024-06-03T16:30:00.339293296Z\",\"submitted_at\":\"2024-06-03T16:30:00.338722766Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":\"1\",\"qty\":null,\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"time_in_force\":\"gtc\",\"limit_price\":\"100000\",\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null}\n";
    public static final String RESPONSE_LIMIT_QUANTITY_ORDER = "{\"id\":\"3333f957-aebe-48f5-a196-720794644ca1\",\"client_order_id\":\"510f787f-9dda-401e-b72f-5177143380b7\",\"created_at\":\"2024-06-03T16:31:59.23323392Z\",\"updated_at\":\"2024-06-03T16:31:59.23367849Z\",\"submitted_at\":\"2024-06-03T16:31:59.23323392Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":null,\"qty\":\"0.0001\",\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"time_in_force\":\"gtc\",\"limit_price\":\"100000\",\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null}\n";

    public static final String MESSAGE_MARKET_NATIONAL_ORDER = "{\"stream\":\"trade_updates\",\"data\":{\"event\":\"fill\",\"timestamp\":\"2024-05-30T16:30:52.602095146Z\",\"order\":{\"id\":\"120bbfe6-125a-49ce-970f-936252a7a5f4\",\"client_order_id\":\"5cc76a75-4e40-43ae-b251-78ab111cbad3\",\"created_at\":\"2024-05-30T16:30:52.599856972Z\",\"updated_at\":\"2024-05-30T16:30:52.626654479Z\",\"submitted_at\":\"2024-05-30T16:30:52.599856972Z\",\"filled_at\":\"2024-05-30T16:30:52.602095146Z\",\"expired_at\":null,\"cancel_requested_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":\"1.27\",\"qty\":null,\"filled_qty\":\"0.000018187\",\"filled_avg_price\":\"68655.172\",\"order_class\":\"\",\"order_type\":\"market\",\"type\":\"market\",\"side\":\"buy\",\"time_in_force\":\"gtc\",\"limit_price\":null,\"stop_price\":null,\"status\":\"filled\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null},\"price\":\"68655.172\",\"qty\":\"0.000018187\",\"position_qty\":\"0.000018141\",\"execution_id\":\"7bc5fae4-6782-4acb-aa93-418487b499ac\"}}";
    public static final String MESSAGE_LIMIT_NATIONAL_ORDER = "{\"stream\":\"trade_updates\",\"data\":{\"event\":\"fill\",\"timestamp\":\"2024-05-30T16:35:16.744951305Z\",\"order\":{\"id\":\"5b6b9242-b363-42d2-8f0a-e64d114a4e1f\",\"client_order_id\":\"7594e18f-5995-4018-ab3c-2070b17f84c7\",\"created_at\":\"2024-05-30T16:35:16.742856837Z\",\"updated_at\":\"2024-05-30T16:35:16.779293142Z\",\"submitted_at\":\"2024-05-30T16:35:16.742856837Z\",\"filled_at\":\"2024-05-30T16:35:16.744951305Z\",\"expired_at\":null,\"cancel_requested_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":\"1\",\"qty\":null,\"filled_qty\":\"0.00001\",\"filled_avg_price\":\"68672.16\",\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"time_in_force\":\"gtc\",\"limit_price\":\"100000\",\"stop_price\":null,\"status\":\"filled\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null},\"price\":\"68672.16\",\"qty\":\"0.00001\",\"position_qty\":\"0.000009975\",\"execution_id\":\"5cb27ce4-8b4c-49fb-be45-ca187841f393\"}}";
    public static final String MESSAGE_LIMIT_QUANTITY_ORDER = "{\"stream\":\"trade_updates\",\"data\":{\"event\":\"fill\",\"timestamp\":\"2024-06-01T07:32:50.938073062Z\",\"order\":{\"id\":\"8126d112-c01b-4d83-8c55-5501b0d3fb2f\",\"client_order_id\":\"8f70416e-25bf-4392-a639-e9244dd2d066\",\"created_at\":\"2024-06-01T07:32:50.933042623Z\",\"updated_at\":\"2024-06-01T07:32:50.976927027Z\",\"submitted_at\":\"2024-06-01T07:32:50.933042623Z\",\"filled_at\":\"2024-06-01T07:32:50.938073062Z\",\"expired_at\":null,\"cancel_requested_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":null,\"qty\":\"0.0001\",\"filled_qty\":\"0.0001\",\"filled_avg_price\":\"67755.779\",\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"time_in_force\":\"gtc\",\"limit_price\":\"100000\",\"stop_price\":null,\"status\":\"filled\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null},\"price\":\"67755.779\",\"qty\":\"0.0001\",\"position_qty\":\"0.00009975\",\"execution_id\":\"ab69a9c8-787d-457b-bae9-83a56d22d39c\"}}\n";

    public static final String RESPONSE_ERROR_NO_ID = "{\"code\":40010001,\"message\":\"order_id is missing\"}";
    public static final String RESPONSE_ERROR_WRONG_ID = "{\"code\":40410000,\"message\":\"order not found for 3333f957-aebe-48f5-a196-720794644ca2\"}";

    public static final String RESPONSE_ERROR_INSUFFISANT_BALANCE_BUY = "{\"available\":\"91.26\",\"balance\":\"91.26\",\"code\":40310000,\"message\":\"insufficient balance for USD (requested: 150000, available: 91.26)\",\"symbol\":\"USD\"}\n";
    public static final String RESPONSE_ERROR_INSUFFISANT_BALANCE_SELL = "{\"available\":\"0.00002783\",\"balance\":\"0.00002783\",\"code\":40310000,\"message\":\"insufficient balance for BTC (requested: 2.157603065, available: 0.00002783)\",\"symbol\":\"USD\"}\n";

    @Mock
    private Logger logger;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        this.orderService = new OrderService("http://localhost:8080/v2/orders", objectMapper, httpRequestService);
        ReflectionTestUtils.setField(orderService, "logger", logger);
    }


    // getOrderById()

    @Test
    @DisplayName("getOrderById: nominal")
    void getOrderByIdNominal() throws IOException {
        // Configurer la réponse simulée
        stubFor(get(urlPathEqualTo("/v2/orders/3333f957-aebe-48f5-a196-720794644ca1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_LIMIT_QUANTITY_ORDER)));

        OrderModel order = orderService.getOrderById("3333f957-aebe-48f5-a196-720794644ca1");

        assertNotNull(order);
        assertEquals("3333f957-aebe-48f5-a196-720794644ca1", order.getId());
        assertEquals(Date.from(Instant.parse("2024-06-03T16:31:59.23323392Z")), order.getCreatedAt());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertEquals("BTC/USD", order.getSymbol());
        assertNull(order.getNotional());
        assertEquals("0.0001", order.getQuantity());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(0, order.getFilledAvgPrice());
        assertEquals("limit", order.getOrderType());
        assertEquals("buy", order.getSide());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals(100000, order.getLimitPrice());
        assertEquals("pending_new", order.getStatus());
    }

    @Test
    @DisplayName("Message to order serialization : error (no ID given)")
    void getOrderByIdErrorNoId() throws IOException {
        // Configurer la réponse simulée
        stubFor(get(urlPathEqualTo("/v2/orders/azerty123456789"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_ERROR_NO_ID)));

        OrderModel order = orderService.getOrderById("azerty123456789");

        assertNull(order);
        org.mockito.Mockito.verify(logger).warn("Order of id '{}' not found: '{}' (code {})",
                "azerty123456789",
                "order_id is missing",
                422);
    }

    @Test
    @DisplayName("Message to order serialization : error (unknown ID given)")
    void getOrderByIdErrorWrongId() throws IOException {
        // Configurer la réponse simulée
        stubFor(get(urlPathEqualTo("/v2/orders/3333f957-aebe-48f5-a196-720794644ca2"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_ERROR_WRONG_ID)));

        OrderModel order = orderService.getOrderById("3333f957-aebe-48f5-a196-720794644ca2");

        assertNull(order);
        org.mockito.Mockito.verify(logger).warn("Order of id '{}' not found: '{}' (code {})",
                "3333f957-aebe-48f5-a196-720794644ca2",
                "order not found for 3333f957-aebe-48f5-a196-720794644ca2",
                404);
    }


    // createXxxYyyOrder()

    @Test
    @DisplayName("Posting Market Notional Order")
    void createMarketNotionalOrder() throws IOException {
        // Configurer la réponse simulée
        stubFor(post(urlPathEqualTo("/v2/orders"))
                .withRequestBody(matchingJsonPath("$.notional", matching(".+")))
                .withRequestBody(matchingJsonPath("$.type", equalTo("market")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_MARKET_NATIONAL_ORDER)));

        OrderModel order = orderService.createMarketNotionalOrder(
                "BTC/USD",
                "1.27",
                OrderSide.BUY,
                TimeInForce.GTC);

        assertNotNull(order);
        assertEquals("5ebe667b-4694-4259-aabe-2eb96a4acdd1", order.getId());
        assertEquals(Date.from(Instant.parse("2024-06-03T16:25:51.118965912Z")), order.getCreatedAt());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1.27", order.getNotional());
        assertNull(order.getQuantity());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(0, order.getFilledAvgPrice());
        assertEquals("market", order.getOrderType());
        assertEquals("buy", order.getSide());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals(0, order.getLimitPrice());
        assertEquals("pending_new", order.getStatus());
    }

    @Test
    @DisplayName("Posting Limit Notional Order")
    void createLimitNotionalOrder() throws IOException {
        // Configurer la réponse simulée
        stubFor(post(urlPathEqualTo("/v2/orders"))
                .withRequestBody(matchingJsonPath("$.notional", matching(".+")))
                .withRequestBody(matchingJsonPath("$.type", equalTo("limit")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_LIMIT_NATIONAL_ORDER)));

        OrderModel order = orderService.createLimitNotionalOrder(
                "BTC/USD",
                "1",
                OrderSide.BUY,
                TimeInForce.GTC,
                "100000");

        assertNotNull(order);
        assertEquals("2cf1dcb8-c814-43ca-848d-cc2aec713a8c", order.getId());
        assertEquals(Date.from(Instant.parse("2024-06-03T16:30:00.338722766Z")), order.getCreatedAt());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1", order.getNotional());
        assertNull(order.getQuantity());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(0, order.getFilledAvgPrice());
        assertEquals("limit", order.getOrderType());
        assertEquals("buy", order.getSide());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals(100000, order.getLimitPrice());
        assertEquals("pending_new", order.getStatus());
    }

    @Test
    @DisplayName("Posting Limit Quantity Order")
    void createLimitQuantityOrder() throws IOException {
        // Configurer la réponse simulée
        stubFor(post(urlPathEqualTo("/v2/orders"))
                .withRequestBody(matchingJsonPath("$.qty", matching(".+")))
                .withRequestBody(matchingJsonPath("$.type", equalTo("limit")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_LIMIT_QUANTITY_ORDER)));

        OrderModel order = orderService.createLimitQuantityOrder(
                "BTC/USD",
                "0.0001",
                OrderSide.BUY,
                TimeInForce.GTC,
                "100000");

        assertNotNull(order);
        assertEquals("3333f957-aebe-48f5-a196-720794644ca1", order.getId());
        assertEquals(Date.from(Instant.parse("2024-06-03T16:31:59.23323392Z")), order.getCreatedAt());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertEquals("BTC/USD", order.getSymbol());
        assertNull(order.getNotional());
        assertEquals("0.0001", order.getQuantity());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(0, order.getFilledAvgPrice());
        assertEquals("limit", order.getOrderType());
        assertEquals("buy", order.getSide());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals(100000, order.getLimitPrice());
        assertEquals("pending_new", order.getStatus());
    }

    @Test
    @DisplayName("Creating order : error case (insuffisant balance when buying)")
    void creatingImpossibleOrderBuy() throws IOException {
        // Configurer la réponse simulée
        stubFor(post(urlPathEqualTo("/v2/orders"))
                .withRequestBody(matchingJsonPath("$.symbol", equalTo("BTC/USD")))
                .withRequestBody(matchingJsonPath("$.side", equalTo("buy")))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_ERROR_INSUFFISANT_BALANCE_BUY)));

        OrderModel order = orderService.createMarketNotionalOrder("BTC/USD", "150000", OrderSide.BUY, TimeInForce.GTC);

        assertNull(order);
        org.mockito.Mockito.verify(logger).warn("Order was not created: '{}' (code {})",
                "insufficient balance for USD (requested: 150000, available: 91.26)",
                403);
    }

    @Test
    @DisplayName("Creating order : error case (insuffisant balance when selling)")
    void creatingImpossibleOrderSell() throws IOException {
        // Configurer la réponse simulée
        stubFor(post(urlPathEqualTo("/v2/orders"))
                .withRequestBody(matchingJsonPath("$.symbol", equalTo("BTC/USD")))
                .withRequestBody(matchingJsonPath("$.side", equalTo("sell")))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_ERROR_INSUFFISANT_BALANCE_SELL)));

        OrderModel order = orderService.createMarketNotionalOrder("BTC/USD", "150000", OrderSide.SELL, TimeInForce.GTC);

        assertNull(order);
        org.mockito.Mockito.verify(logger).warn("Order was not created: '{}' (code {})",
                "insufficient balance for BTC (requested: 2.157603065, available: 0.00002783)",
                403);
    }


    // messageToOrder()

    @Test
    @DisplayName("Message to order serialization : nominal - Market National")
    void messageToOrderNominalMarketNational() {
        OrderModel order = orderService.messageToOrder(MESSAGE_MARKET_NATIONAL_ORDER);

        assertNotNull(order);
        assertEquals("120bbfe6-125a-49ce-970f-936252a7a5f4", order.getId());
        assertEquals(Date.from(Instant.parse("2024-05-30T16:30:52.599856972Z")), order.getCreatedAt());
        assertEquals(Date.from(Instant.parse("2024-05-30T16:30:52.602095146Z")), order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1.27", order.getNotional());
        assertNull(order.getQuantity());
        assertEquals(0.000018187, order.getFilledQuantity());
        assertEquals(68655.172, order.getFilledAvgPrice());
        assertEquals("market", order.getOrderType());
        assertEquals("buy", order.getSide());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals(0, order.getLimitPrice());
        assertEquals("filled", order.getStatus());
    }

    @Test
    @DisplayName("Message to order serialization : nominal - Limit National")
    void messageToOrderNominalLimitNational() {
        OrderModel order = orderService.messageToOrder(MESSAGE_LIMIT_NATIONAL_ORDER);

        assertNotNull(order);
        assertEquals("5b6b9242-b363-42d2-8f0a-e64d114a4e1f", order.getId());
        assertEquals(Date.from(Instant.parse("2024-05-30T16:35:16.742856837Z")), order.getCreatedAt());
        assertEquals(Date.from(Instant.parse("2024-05-30T16:35:16.744951305Z")), order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1", order.getNotional());
        assertNull(order.getQuantity());
        assertEquals(0.00001, order.getFilledQuantity());
        assertEquals(68672.16, order.getFilledAvgPrice());
        assertEquals("limit", order.getOrderType());
        assertEquals("buy", order.getSide());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals(100000, order.getLimitPrice());
        assertEquals("filled", order.getStatus());
    }

    @Test
    @DisplayName("Message to order serialization : nominal - Limit Quantity")
    void messageToOrderNominalLimitQuantity() {
        OrderModel order = orderService.messageToOrder(MESSAGE_LIMIT_QUANTITY_ORDER);

        assertNotNull(order);
        assertEquals("8126d112-c01b-4d83-8c55-5501b0d3fb2f", order.getId());
        assertEquals(Date.from(Instant.parse("2024-06-01T07:32:50.933042623Z")), order.getCreatedAt());
        assertEquals(Date.from(Instant.parse("2024-06-01T07:32:50.938073062Z")), order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertEquals("BTC/USD", order.getSymbol());
        assertNull(order.getNotional());
        assertEquals("0.0001", order.getQuantity());
        assertEquals(0.0001, order.getFilledQuantity());
        assertEquals(67755.779, order.getFilledAvgPrice());
        assertEquals("limit", order.getOrderType());
        assertEquals("buy", order.getSide());
        assertEquals("gtc", order.getTimeInForce());
        assertEquals(100000, order.getLimitPrice());
        assertEquals("filled", order.getStatus());
    }

    @Test
    @DisplayName("Message to order serialization : error")
    void messageToOrderError() {
        String badMessage = "bad message";
        assertThrows(RuntimeException.class, () -> orderService.messageToOrder(badMessage));
    }

}