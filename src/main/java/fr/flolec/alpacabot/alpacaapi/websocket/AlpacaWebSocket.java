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

    @Value("${WEBSOCKET_URI}")
    private String webSocketUri;

    private final OkHttpClient okHttpClient;
    private final AlpacaWebSocketListener alpacaWebSocketListener;
    private WebSocket webSocket;

    @Autowired
    public AlpacaWebSocket(OkHttpClient okHttpClient,
                           AlpacaWebSocketListener alpacaWebSocketListener) {
        this.okHttpClient = okHttpClient;
        this.alpacaWebSocketListener = alpacaWebSocketListener;
    }

    @PostConstruct
    public void openSocket() {
        Request request = new Request.Builder()
                .url(webSocketUri)
                .build();
        webSocket = okHttpClient.newWebSocket(request, alpacaWebSocketListener);
    }

    public void closeSocket() {
        webSocket.close(1000, "Closed web socket");
    }
}
