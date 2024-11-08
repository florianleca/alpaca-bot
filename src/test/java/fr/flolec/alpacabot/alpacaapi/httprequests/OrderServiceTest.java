package fr.flolec.alpacabot.alpacaapi.httprequests;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(OrderService.class)
@ContextConfiguration(classes = {
        RestClientConfiguration.class,
        OrderService.class})
public class OrderServiceTest {

    public static final String RESPONSE_MARKET_NATIONAL_ORDER = "{\"id\":\"b35378cd-a236-4e70-a83b-2acbba80e04c\",\"client_order_id\":\"bd69e5b2-05ac-4576-9792-9f30f72d93e7\",\"created_at\":\"2024-11-05T19:57:41.980769861Z\",\"updated_at\":\"2024-11-05T19:57:41.981759811Z\",\"submitted_at\":\"2024-11-05T19:57:41.980769861Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":\"1\",\"qty\":null,\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"market\",\"type\":\"market\",\"side\":\"buy\",\"position_intent\":\"buy_to_open\",\"time_in_force\":\"gtc\",\"limit_price\":null,\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null,\"expires_at\":\"2025-02-03T21:00:00Z\"}";
    public static final String RESPONSE_LIMIT_NATIONAL_ORDER = "{\"id\":\"8586ac05-1a77-4b2a-a878-b22fb0890741\",\"client_order_id\":\"2fdb2751-d6cb-4111-b5b1-ea667cd9560d\",\"created_at\":\"2024-11-06T17:42:21.441202163Z\",\"updated_at\":\"2024-11-06T17:42:21.442866623Z\",\"submitted_at\":\"2024-11-06T17:42:21.441202163Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":\"1\",\"qty\":null,\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"position_intent\":\"buy_to_open\",\"time_in_force\":\"gtc\",\"limit_price\":\"10000\",\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null,\"expires_at\":\"2025-02-04T21:00:00Z\"}";
    public static final String RESPONSE_LIMIT_QUANTITY_ORDER = "{\"id\":\"84e399a2-593e-4579-bd83-0d87205b24b1\",\"client_order_id\":\"8aa356e7-bcac-4ddd-94b2-112baf152182\",\"created_at\":\"2024-11-06T17:47:50.80968361Z\",\"updated_at\":\"2024-11-06T17:47:50.81076662Z\",\"submitted_at\":\"2024-11-06T17:47:50.80968361Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":null,\"qty\":\"0.1\",\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"position_intent\":\"buy_to_open\",\"time_in_force\":\"gtc\",\"limit_price\":\"50\",\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null,\"expires_at\":\"2025-02-04T21:00:00Z\"}";

    public static final String RESPONSE_ERROR_WRONG_ID = "{\"code\":40410000,\"message\":\"order not found for azerty\"}";

    public static final String RESPONSE_ERROR_INSUFFISANT_BALANCE = "{\"available\":\"87.84\",\"balance\":\"87.84\",\"code\":40310000,\"message\":\"insufficient balance for USD (requested: 1000, available: 87.84)\",\"symbol\":\"USD\"}\"";


    @Value("${ALPACA_API_ORDERS_URI}")
    private String uri;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "logger", logger);
    }

    @Test
    @DisplayName("getOrderById: nominal -> orderRetrieved")
    void getOrderById_nominal_orderRetrieved() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri + "/b35378cd-a236-4e70-a83b-2acbba80e04c"))
                .andRespond(withSuccess(RESPONSE_MARKET_NATIONAL_ORDER, MediaType.APPLICATION_JSON));

        OrderModel order = orderService.getOrderById("b35378cd-a236-4e70-a83b-2acbba80e04c");

        assertNotNull(order);
        assertEquals("b35378cd-a236-4e70-a83b-2acbba80e04c", order.getId());
        assertEquals(Date.from(Instant.parse("2024-11-05T19:57:41.980769861Z")), order.getCreatedAt());
        assertNull(order.getFilledAt());
        assertNull(order.getCanceledAt());
        assertEquals("BTC/USD", order.getSymbol());
        assertEquals("1", order.getNotional());
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
    @DisplayName("getOrderById: bad ID error -> null order & logged error")
    void getOrderById_badIdError_nullOrderAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri + "/azerty123456789"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(RESPONSE_ERROR_WRONG_ID)
                        .contentType(MediaType.APPLICATION_JSON));

        OrderModel order = orderService.getOrderById("azerty123456789");

        assertNull(order);
        verify(logger).warn("Could not retrieve order of id '{}': {}",
                "azerty123456789",
                "422 Unprocessable Entity: \"" + RESPONSE_ERROR_WRONG_ID + "\"");
    }

    @Test
    @DisplayName("createMarketNotionalOrder: nominal -> order created and logged")
    void createMarketNotionalOrder_nominal_orderCreatedAndLogged() {
        mockRestServiceServer.expect(method(HttpMethod.POST))
                .andExpect(requestTo(uri))
                .andRespond(withSuccess(RESPONSE_MARKET_NATIONAL_ORDER, MediaType.APPLICATION_JSON));

        OrderModel orderModel = orderService.createMarketNotionalOrder("BTC/USD", "1", OrderSide.BUY, TimeInForce.GTC);

        assertNotNull(orderModel);
        assertEquals("BTC/USD", orderModel.getSymbol());
        assertEquals("1", orderModel.getNotional());
        assertNull(orderModel.getQuantity());
        assertEquals("buy", orderModel.getSide());
        assertEquals("gtc", orderModel.getTimeInForce());
        assertEquals(0, orderModel.getLimitPrice());

        verify(logger).info("An order has been created: {}", RESPONSE_MARKET_NATIONAL_ORDER);
    }

    @Test
    @DisplayName("createLimitNotionalOrder: nominal -> order created and logged")
    void createLimitNotionalOrder_nominal_orderCreatedAndLogged() {
        mockRestServiceServer.expect(method(HttpMethod.POST))
                .andExpect(requestTo(uri))
                .andRespond(withSuccess(RESPONSE_LIMIT_NATIONAL_ORDER, MediaType.APPLICATION_JSON));

        OrderModel orderModel = orderService.createLimitNotionalOrder("BTC/USD", "1", OrderSide.BUY, TimeInForce.GTC, "10000");

        assertNotNull(orderModel);
        assertEquals("BTC/USD", orderModel.getSymbol());
        assertEquals("1", orderModel.getNotional());
        assertNull(orderModel.getQuantity());
        assertEquals("buy", orderModel.getSide());
        assertEquals("gtc", orderModel.getTimeInForce());
        assertEquals(10000, orderModel.getLimitPrice());

        verify(logger).info("An order has been created: {}", RESPONSE_LIMIT_NATIONAL_ORDER);
    }

    @Test
    @DisplayName("createLimitQuantityOrder: nominal -> order created and logged")
    void createLimitQuantityOrder_nominal_orderCreatedAndLogged() {
        mockRestServiceServer.expect(method(HttpMethod.POST))
                .andExpect(requestTo(uri))
                .andRespond(withSuccess(RESPONSE_LIMIT_QUANTITY_ORDER, MediaType.APPLICATION_JSON));

        OrderModel orderModel = orderService.createLimitQuantityOrder("BTC/USD", "0.1", OrderSide.BUY, TimeInForce.GTC, "50");

        assertNotNull(orderModel);
        assertEquals("BTC/USD", orderModel.getSymbol());
        assertNull(orderModel.getNotional());
        assertEquals("0.1", orderModel.getQuantity());
        assertEquals("buy", orderModel.getSide());
        assertEquals("gtc", orderModel.getTimeInForce());
        assertEquals(50, orderModel.getLimitPrice());

        verify(logger).info("An order has been created: {}", RESPONSE_LIMIT_QUANTITY_ORDER);
    }

    @Test
    @DisplayName("createOrder: error -> null order & logged error")
    void createOrder_error_nullOrderAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.POST))
                .andExpect(requestTo(uri))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .body(RESPONSE_ERROR_INSUFFISANT_BALANCE)
                        .contentType(MediaType.APPLICATION_JSON));

        OrderModel orderModel = orderService.createMarketNotionalOrder("BTC/USD", "1000", OrderSide.BUY, TimeInForce.GTC);

        assertNull(orderModel);
        verify(logger).warn("Order could not be created: {}", "403 Forbidden: \"" + RESPONSE_ERROR_INSUFFISANT_BALANCE + "\"");
    }

}