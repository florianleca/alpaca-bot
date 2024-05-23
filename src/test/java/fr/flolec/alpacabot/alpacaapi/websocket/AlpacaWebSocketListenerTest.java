package fr.flolec.alpacabot.alpacaapi.websocket;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.strategies.strategy1.Strategy1Service;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlpacaWebSocketListenerTest {

    private final String messageText = "{\"stream\":\"trade_updates\",\"data\":{\"event\":\"fill\",\"timestamp\":\"2024-02-09T17:29:12.170675394Z\",\"order\":{\"id\":\"8a5f7512-3c6c-4d60-b71d-181eac647588\",\"client_order_id\":\"c640328c-cd54-4d37-a07c-91308723ae5d\",\"created_at\":\"2024-02-09T17:29:12.167882704Z\",\"updated_at\":\"2024-02-09T17:29:12.191000233Z\",\"submitted_at\":\"2024-02-09T17:29:12.166468875Z\",\"filled_at\":\"2024-02-09T17:29:12.170675394Z\",\"expired_at\":null,\"cancel_requested_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"ce414735-ba65-43c8-be76-7cb78ed2413d\",\"symbol\":\"SUSHI/USD\",\"asset_class\":\"crypto\",\"notional\":\"1\",\"qty\":null,\"filled_qty\":\"0.881834215\",\"filled_avg_price\":\"1.134\",\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"time_in_force\":\"ioc\",\"limit_price\":\"1.134\",\"stop_price\":null,\"status\":\"filled\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null},\"price\":\"1.134\",\"qty\":\"0.881834215\",\"position_qty\":\"0.879629629\",\"execution_id\":\"690fc59a-0bd5-4a34-8dc2-d15002e268fe\"}}";
    @Mock
    private WebSocket webSocket;
    @Mock
    private Response response;
    @Mock
    private Logger logger;
    @Mock
    private OrderService orderService;
    @Mock
    private Strategy1Service strategy1Service;
    @InjectMocks
    private AlpacaWebSocketListener alpacaWebSocketListener;

    @BeforeEach
    public void setUp() {
        alpacaWebSocketListener.setLogger(logger);
    }

    @Test
    public void testOnOpen() {
        alpacaWebSocketListener.onOpen(webSocket, response);
        verify(logger).info("WebSocket connected");
        verify(webSocket, times(2)).send(anyString());
    }

    @Test
    void onMessageText() {
        alpacaWebSocketListener.onMessage(webSocket, messageText);
        verify(logger).info("Received message: {}", messageText);
    }

    @Test
    void onBadMessageBytes() {
        String badMessage = "toto";
        alpacaWebSocketListener.onMessage(webSocket, ByteString.encodeUtf8(badMessage));
        verify(logger).info("Received bytes: {}", badMessage);
        verify(orderService, times(0)).messageToOrder(badMessage);
    }

    @Test
    void onGoodMessageBytes() {
        OrderModel order = new OrderModel();
        when(orderService.messageToOrder(messageText)).thenReturn(order);

        alpacaWebSocketListener.onMessage(webSocket, ByteString.encodeUtf8(messageText));
        verify(logger).info("Received bytes: {}", messageText);
        verify(orderService).messageToOrder(messageText);
        verify(strategy1Service).processFilledOrder(order);
    }

    @Test
    public void testOnClosing() {
        int code = 1000;
        String reason = "Normal closure";
        alpacaWebSocketListener.onClosing(webSocket, code, reason);
        verify(webSocket).close(1000, null);
        verify(logger).info("WebSocket closing: {} - {}", code, reason);
    }

}