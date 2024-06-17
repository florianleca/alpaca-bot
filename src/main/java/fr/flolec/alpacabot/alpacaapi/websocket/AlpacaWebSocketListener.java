package fr.flolec.alpacabot.alpacaapi.websocket;

import lombok.Setter;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Setter
public class AlpacaWebSocketListener extends WebSocketListener {

    @Value("${ALPACA_API_KEY_ID}")
    private String keyId;

    @Value("${ALPACA_API_SECRET_KEY}")
    private String secretKey;

    private Logger logger;
    private List<String> untreatedMessages;

    public List<String> getUntreatedMessages() {
        return untreatedMessages;
    }

    @Autowired
    public AlpacaWebSocketListener() {
        this.logger = LoggerFactory.getLogger(AlpacaWebSocketListener.class);
        this.untreatedMessages = new ArrayList<>();
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
    @Async
    public void onMessage(@NotNull WebSocket webSocket, ByteString bytes) {
        String message = bytes.utf8();
        logger.info("Received bytes: {}", message);
        if (message.contains("\"event\":\"fill\"")) {
            untreatedMessages.add(message);
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, @NotNull String reason) {
        webSocket.close(1000, null);
        logger.info("WebSocket closing: {} - {}", code, reason);
    }

    public void clearMessages() {
        untreatedMessages.clear();
    }
}
