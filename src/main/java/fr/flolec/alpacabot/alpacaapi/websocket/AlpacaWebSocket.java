package fr.flolec.alpacabot.alpacaapi.websocket;


import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AlpacaWebSocket {

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private AlpacaWebSocketListener alpacaWebSocketListener;

    @Value("${WEBSOCKET_URI}")
    private String webSocketUri;

    @PostConstruct
    public void init() {
        Request request = new Request.Builder()
                .url(webSocketUri)
                .build();
        WebSocket webSocket = okHttpClient.newWebSocket(request, alpacaWebSocketListener);
        // Close the WebSocket when done
        // webSocket.close(1000, "Goodbye, WebSocket!");
    }
}

