package fr.flolec.alpacabot.alpacaapi.websocket;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import okio.ByteString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = AlpacaBotApplication.class)
public class AlpacaWebSocketListenerTest {

    @Autowired
    private AlpacaWebSocketListener alpacaWebSocketListener;

    @Autowired
    private AlpacaWebSocket alpacaWebSocket;

    private final String messageText = "{\"stream\":\"trade_updates\",\"data\":{\"event\":\"fill\",\"timestamp\":\"2024-02-09T17:29:12.170675394Z\",\"order\":{\"id\":\"8a5f7512-3c6c-4d60-b71d-181eac647588\",\"client_order_id\":\"c640328c-cd54-4d37-a07c-91308723ae5d\",\"created_at\":\"2024-02-09T17:29:12.167882704Z\",\"updated_at\":\"2024-02-09T17:29:12.191000233Z\",\"submitted_at\":\"2024-02-09T17:29:12.166468875Z\",\"filled_at\":\"2024-02-09T17:29:12.170675394Z\",\"expired_at\":null,\"cancel_requested_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"ce414735-ba65-43c8-be76-7cb78ed2413d\",\"symbol\":\"SUSHI/USD\",\"asset_class\":\"crypto\",\"notional\":\"1\",\"qty\":null,\"filled_qty\":\"0.881834215\",\"filled_avg_price\":\"1.134\",\"order_class\":\"\",\"order_type\":\"limit\",\"type\":\"limit\",\"side\":\"buy\",\"time_in_force\":\"ioc\",\"limit_price\":\"1.134\",\"stop_price\":null,\"status\":\"filled\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null},\"price\":\"1.134\",\"qty\":\"0.881834215\",\"position_qty\":\"0.879629629\",\"execution_id\":\"690fc59a-0bd5-4a34-8dc2-d15002e268fe\"}}";

    private final ByteString messageBytes = ByteString.decodeHex("7b2273747265616d223a2274726164655f75706461746573222c2264617461223a7b226576656e74223a2266696c6c222c2274696d657374616d70223a22323032342d30322d30395431373a32393a31322e3137303637353339345a222c226f72646572223a7b226964223a2238613566373531322d336336632d346436302d623731642d313831656163363437353838222c22636c69656e745f6f726465725f6964223a2263363430333238632d636435342d346433372d613037632d393133303837323361653564222c22637265617465645f6174223a22323032342d30322d30395431373a32393a31322e3136373838323730345a222c22757064617465645f6174223a22323032342d30322d30395431373a32393a31322e3139313030303233335a222c227375626d69747465645f6174223a22323032342d30322d30395431373a32393a31322e3136363436383837355a222c2266696c6c65645f6174223a22323032342d30322d30395431373a32393a31322e3137303637353339345a222c22657870697265645f6174223a6e756c6c2c2263616e63656c5f7265717565737465645f6174223a6e756c6c2c2263616e63656c65645f6174223a6e756c6c2c226661696c65645f6174223a6e756c6c2c227265706c616365645f6174223a6e756c6c2c227265706c616365645f6279223a6e756c6c2c227265706c61636573223a6e756c6c2c2261737365745f6964223a2263653431343733352d626136352d343363382d626537362d376362373865643234313364222c2273796d626f6c223a2253555348492f555344222c2261737365745f636c617373223a2263727970746f222c226e6f74696f6e616c223a2231222c22717479223a6e756c6c2c2266696c6c65645f717479223a22302e383831383334323135222c2266696c6c65645f6176675f7072696365223a22312e313334222c226f726465725f636c617373223a22222c226f726465725f74797065223a226c696d6974222c2274797065223a226c696d6974222c2273696465223a22627579222c2274696d655f696e5f666f726365223a22696f63222c226c696d69745f7072696365223a22312e313334222c2273746f705f7072696365223a6e756c6c2c22737461747573223a2266696c6c6564222c22657874656e6465645f686f757273223a66616c73652c226c656773223a6e756c6c2c22747261696c5f70657263656e74223a6e756c6c2c22747261696c5f7072696365223a6e756c6c2c2268776d223a6e756c6c7d2c227072696365223a22312e313334222c22717479223a22302e383831383334323135222c22706f736974696f6e5f717479223a22302e383739363239363239222c22657865637574696f6e5f6964223a2236393066633539612d306264352d346133342d386463322d643135303032653236386665227d7d");

    @BeforeEach
    void setUp() {
        Logger loggerMock = Mockito.mock(Logger.class);
        alpacaWebSocketListener.setLogger(loggerMock);
    }

    @AfterEach
    void tearDown() {
        alpacaWebSocketListener.setLogger(LoggerFactory.getLogger(AlpacaWebSocketListener.class));
    }

    @Test
    void onMessageText() {
        alpacaWebSocketListener.onMessage(alpacaWebSocket.getWebSocket(), messageText);
        verify(alpacaWebSocketListener.getLogger(), times(1)).info("Received message: {}", messageText);
    }

    @Test
    void onMessageBytes() {
        alpacaWebSocketListener.onMessage(alpacaWebSocket.getWebSocket(), messageBytes);
        verify(alpacaWebSocketListener.getLogger(), times(1)).info("Received bytes: {}", messageText);
    }

//    @Test
//    public void testOnMessage() throws InterruptedException {
//        // Créer un mock de WebSocket et de OrderService
//        AlpacaWebSocketListener webSocket = Mockito.mock(AlpacaWebSocketListener.class);
//        OrderService orderService = Mockito.mock(OrderService.class);
//
//
//        // Capturer les arguments passés au service d'ordre
//        ArgumentCaptor<OrderModel> orderCaptor = ArgumentCaptor.forClass(OrderModel.class);
//
//        // Créer un verrou de comptage pour attendre l'exécution de la méthode asynchrone
//        CountDownLatch latch = new CountDownLatch(1);
//
//        // Appeler la méthode asynchrone
//        alpacaWebSocketListener.onMessage(alpacaWebSocket.getWebSocket(), ByteString.encodeUtf8("{\"event\":\"fill\"}"));
//
//        // Attendre que la méthode asynchrone soit exécutée (ou échoue après un certain délai)
//        latch.await(5, TimeUnit.SECONDS);
//
//        // Vérifier que le service d'ordre a été appelé avec les arguments attendus
//        Mockito.verify(orderService).messageToOrder(Mockito.anyString());
//        Mockito.verify(orderService).processFilledOrder(orderCaptor.capture());
//        assertEquals("{\"event\":\"fill\"}", orderCaptor.getValue().getMessage()); // Assurez-vous que le message est correct
//
//        // Vous pouvez également vérifier d'autres comportements ici
//    }
}