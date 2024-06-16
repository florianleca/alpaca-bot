package fr.flolec.alpacabot.alpacaapi.websocket;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlpacaWebSocketTest {

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private AlpacaWebSocketListener alpacaWebSocketListener;

    @Mock
    private WebSocket webSocket;

    @InjectMocks
    private AlpacaWebSocket alpacaWebSocket;

    @BeforeEach
    public void setUp() {
        // Set the webSocketUri using ReflectionTestUtils because it's injected via @Value
        ReflectionTestUtils.setField(alpacaWebSocket, "webSocketUri", "wss://testUri");
    }

    @Test
    public void openSocket() {
        when(okHttpClient.newWebSocket(any(Request.class), eq(alpacaWebSocketListener))).thenReturn(webSocket);
        assertNull(ReflectionTestUtils.getField(alpacaWebSocket, "webSocket"));

        alpacaWebSocket.openSocket();

        verify(okHttpClient, times(1)).newWebSocket(any(Request.class), eq(alpacaWebSocketListener));
        assertEquals(webSocket, ReflectionTestUtils.getField(alpacaWebSocket, "webSocket"));
    }

    @Test
    public void closeSocket() {
        ReflectionTestUtils.setField(alpacaWebSocket, "webSocket", webSocket);

        alpacaWebSocket.closeSocket();

        verify(webSocket, times(1)).close(1000, "Closed web socket");
    }

}