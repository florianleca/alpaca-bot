package fr.flolec.alpacabot.alpacaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@ClientEndpoint
public class AlpacaWebSocketService {

    private final ObjectMapper objectMapper;
    @Value("${ALPACA_WEBSOCKET_DATA_URI_CRYPTO}")
    public String webSocketEndpoint;
    @Value("${ALPACA_API_KEY_ID}")
    private String keyId;
    @Value("${ALPACA_API_SECRET_KEY}")
    private String secretKey;
    private Session session;

    public AlpacaWebSocketService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void connectToWebSocket() throws DeploymentException, IOException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(webSocketEndpoint));
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        log.info("Alpaca WebSocket connection established");
        this.session = session;
        authenticateToCryptoWebsocket();
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Received message: {}", message);
    }

    private void authenticateToCryptoWebsocket() throws IOException {
        Map<String, String> authMap = new HashMap<>();
        authMap.put("action", "auth");
        authMap.put("key", keyId);
        authMap.put("secret", secretKey);
        String authMessage = objectMapper.writeValueAsString(authMap);

        log.info("Trying to authenticate to Alpaca Crypto WebSocket...");
        sendMessage(authMessage);
    }

    public void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

}

