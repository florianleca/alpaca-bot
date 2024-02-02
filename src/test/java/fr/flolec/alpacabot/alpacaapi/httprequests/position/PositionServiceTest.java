package fr.flolec.alpacabot.alpacaapi.httprequests.position;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import fr.flolec.alpacabot.alpacaapi.websocket.AlpacaWebSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AlpacaBotApplication.class)
public class PositionServiceTest {

    private final Logger logger = LoggerFactory.getLogger(PositionServiceTest.class);

    @Autowired
    private PositionService positionService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlpacaWebSocket alpacaWebSocket;

    @BeforeEach
    void closeSocket() {
        alpacaWebSocket.closeSocket();
    }

    @AfterEach
    void openSocket() {
        alpacaWebSocket.openSocket();
    }

    @Test
    @DisplayName("Positions values aren't null")
    void getAllOpenPositions1() throws IOException {
        List<PositionModel> positionsList = positionService.getAllOpenPositions();
        if (!positionsList.isEmpty()) {
            positionsList.forEach(position -> {
                assertNotNull(position.getAssetClass());
                assertNotNull(position.getAverageEntryPrice());
                assertNotNull(position.getAssetId());
                assertNotNull(position.getCurrentPrice());
                assertNotNull(position.getChangeToday());
                assertNotNull(position.getCostBasis());
                assertNotNull(position.getExchange());
                assertNotNull(position.getLastDayPrice());
                assertNotNull(position.getMarketValue());
                assertNotNull(position.getSymbol());
                assertNotNull(position.getQuantity());
                assertNotNull(position.getQuantityAvailable());
                assertNotNull(position.getSide());
            });
        }
    }

    @Test
    @DisplayName("PositionModel market values checked")
    void getAllOpenPositions2() throws IOException {
        List<PositionModel> positionsList = positionService.getAllOpenPositions();
        if (!positionsList.isEmpty()) {
            positionsList.forEach(position -> {
                double currentPrice = Double.parseDouble(position.getCurrentPrice());
                double quantity = Double.parseDouble(position.getQuantity());
                double marketValue = Double.parseDouble(position.getMarketValue());
                logger.info("{} - Market value: ${} | Qty * current price: ${}", position.getSymbol(), marketValue, currentPrice * quantity);
                assertEquals(currentPrice * quantity, marketValue, currentPrice / 100);
            });
        }
    }

    @Test
    @DisplayName("Liquidate percentage of position")
    void liquidatePositionByPercentage() throws IOException, InterruptedException {
        // Acheter du BTC et attendre que l'ordre soit filled
        orderService.createMarketNotionalOrder("MKR/USD", "1", OrderSide.BUY, TimeInForce.GTC);
        Thread.sleep(250);
        // Récupérer la nouvelle quantité de la position
        PositionModel positionModel = positionService.getAnOpenPosition("MKR/USD");
        System.out.println(positionModel);
        double afterBuyQty = Double.parseDouble(positionModel.getQuantity());
        // Liquider la moitié de la position
        positionService.liquidatePositionByPercentage("MKR/USD", 50);
        Thread.sleep(250);
        // Récupérer la nouvelle quantité de la position
        double afterLiquidationQty = Double.parseDouble(positionService.getAnOpenPosition("MKR/USD").getQuantity());
        // S'assurer que la quantité a baissé de moitié
        assertEquals(afterBuyQty / 2, afterLiquidationQty, afterBuyQty / 100);
    }

    @Test
    @DisplayName("Liquidate quantity of position")
    void liquidatePositionByQuantity() throws IOException, InterruptedException {
        // Acheter du BTC et attendre que l'ordre soit filled
        orderService.createMarketNotionalOrder("ETH/USD", "1", OrderSide.BUY, TimeInForce.GTC);
        Thread.sleep(250);
        // Récupérer la nouvelle quantité de la position
        double afterBuyQty = Double.parseDouble(positionService.getAnOpenPosition("ETH/USD").getQuantity());
        // Liquider la moitié de la position
        positionService.liquidatePositionByQuantity("ETH/USD", afterBuyQty / 2);
        Thread.sleep(250);
        // Récupérer la nouvelle quantité de la position
        double afterLiquidationQty = Double.parseDouble(positionService.getAnOpenPosition("ETH/USD").getQuantity());
        // S'assurer que la quantité a baissé de moitié
        assertEquals(afterBuyQty / 2, afterLiquidationQty, afterBuyQty / 100);
    }

}