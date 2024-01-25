package fr.flolec.alpacabot.alpacaapi.websocket;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AlpacaWebSocketListener extends WebSocketListener {

    private final String keyId;
    private final String secretKey;
    private final OrderService orderService;
    private final Logger logger = LoggerFactory.getLogger(AlpacaWebSocketListener.class);

    @Autowired
    public AlpacaWebSocketListener(@Value("${ALPACA_API_KEY_ID}") String keyId,
                                   @Value("${ALPACA_API_SECRET_KEY}") String secretKey,
                                   OrderService orderService) {
        this.keyId = keyId;
        this.secretKey = secretKey;
        this.orderService = orderService;
    }

    @Override
    public void onOpen(WebSocket webSocket, @NotNull Response response) {
        logger.info("WebSocket connected");
        String authJson = "{\n" +
                "  \"action\": \"auth\",\n" +
                "  \"key\": \"" + keyId + "\",\n" +
                "  \"secret\": \"" + secretKey + "\"\n" +
                "}";
        webSocket.send(authJson);

        String subscribeJson = """
                {
                  "action": "listen",
                  "data": {
                    "streams": ["trade_updates"]
                  }
                }""";
        webSocket.send(subscribeJson);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        logger.info("Received message: {}", text);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, ByteString bytes) {
        String message = bytes.utf8();
        logger.info("Received bytes: {}", message);
        if (message.contains("\"event\":\"fill\"")) {
            orderService.fillOrder(message);
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, @NotNull String reason) {
        webSocket.close(1000, null);
        logger.info("WebSocket closing: {} - {}", code, reason);
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }

}
