package fr.flolec.alpacabot.position;

import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PositionServiceTest {

    private final Logger logger = LoggerFactory.getLogger(PositionServiceTest.class);
    @Autowired
    private PositionService positionService;
    @Autowired
    private OrderService orderService;

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
                logger.info(position.getSymbol() + " - Market value: " + marketValue + " | Qty * current price: " + currentPrice * quantity);
                assertEquals(currentPrice * quantity, marketValue, currentPrice / 100);
            });
        }
    }

    @Test
    @DisplayName("Liquidate percentage of position")
    void liquidatePositionByPercentage() throws IOException {
        // On liquide la position
        positionService.liquidatePositionByPercentage("DOGE/USD", 100);
        // On recherche la position et on s'assure que la quantité est nulle
        PositionModel position = positionService.getAnOpenPosition("DOGE/USD");
        assertNull(position.getQuantity());
        // On achète et on s'assure que la quantité et non nulle
        orderService.createMarketNotionalOrder("DOGE/USD", "1", OrderSide.BUY, TimeInForce.GTC);
        position = positionService.getAnOpenPosition("DOGE/USD");
        assertNotNull(position.getQuantity());
        double initialQuantity = Double.parseDouble(position.getQuantity());
        // On liquide 50% s'assure que la quantité a baissé de moitié
        positionService.liquidatePositionByPercentage("DOGE/USD", 50);
        position = positionService.getAnOpenPosition("DOGE/USD");
        double halfQuantity = Double.parseDouble(position.getQuantity());
        assertEquals(initialQuantity / 2, halfQuantity, initialQuantity / 100);
        // On liquide le restant de la position
        positionService.liquidatePositionByPercentage("DOGE/USD", 100);
    }

    @Test
    @DisplayName("Liquidate quantity of position")
    void liquidatePositionByQuantity() throws IOException {
        // On liquide la position
        positionService.liquidatePositionByPercentage("DOGE/USD", 100);
        // On recherche la position et on s'assure que la quantité est nulle
        PositionModel position = positionService.getAnOpenPosition("DOGE/USD");
        assertNull(position.getQuantity());
        // On achète et on s'assure que la quantité et non nulle
        orderService.createMarketNotionalOrder("DOGE/USD", "1", OrderSide.BUY, TimeInForce.GTC);
        position = positionService.getAnOpenPosition("DOGE/USD");
        assertNotNull(position.getQuantity());
        double initialQuantity = Double.parseDouble(position.getQuantity());
        // On liquide la moitié de la quantité et on s'assure de la baisse
        double halfQuantity = initialQuantity / 2;
        positionService.liquidatePositionByQuantity("DOGE/USD", halfQuantity);
        position = positionService.getAnOpenPosition("DOGE/USD");
        double shouldBeHalfQuantity = Double.parseDouble(position.getQuantity());
        assertEquals(halfQuantity, shouldBeHalfQuantity, initialQuantity / 100);
        // On liquide le restant de la position
        positionService.liquidatePositionByPercentage("DOGE/USD", 100);
    }

}