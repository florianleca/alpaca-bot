package fr.flolec.alpacabot.bar;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
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
public class BarServiceTest {

    private final Logger logger = LoggerFactory.getLogger(BarServiceTest.class);

    @Autowired
    private BarService barService;

    @Autowired
    private AssetService assetService;

    @Test
    @DisplayName("1 daily bar in the last 24 hours")
    void getHistoricalBars1Daily24Hours() throws IOException {
        AssetModel unAsset = assetService.getAssetsList().get(0);
        List<BarModel> bars = barService.getHistoricalBars(unAsset, BarTimeFrame.DAY1, 24, PeriodLengthUnit.HOUR);
        logger.info("Number of daily bars in the last 24h: " + bars.size());
        assertEquals(1, bars.size());
    }

    @Test
    @DisplayName("7 daily bar in the last week")
    void getHistoricalBars7Daily1Week() throws IOException {
        AssetModel unAsset = assetService.getAssetsList().get(0);
        List<BarModel> bars = barService.getHistoricalBars(unAsset, BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK);
        logger.info("Number of daily bars in the last week: " + bars.size());
        assertEquals(7, bars.size());
    }

    @Test
    @DisplayName("Local max locals lower than global max")
    void getMaxHighOnPeriod() throws IOException {
        AssetModel unAsset = assetService.getAssetsList().get(0);
        double max1minDuring1day = barService.getMaxHighOnPeriod(unAsset, BarTimeFrame.MIN1, 1, PeriodLengthUnit.DAY);
        double max1hourDuring1week = barService.getMaxHighOnPeriod(unAsset, BarTimeFrame.HOUR1, 1, PeriodLengthUnit.WEEK);
        double max1dayDuring1month = barService.getMaxHighOnPeriod(unAsset, BarTimeFrame.DAY1, 1, PeriodLengthUnit.MONTH);
        logger.info("Maximum values of " + unAsset.getSymbol() + " during the last...");
        logger.info("...day: $" + max1minDuring1day);
        logger.info("...week: $" + max1hourDuring1week);
        logger.info("...month: $" + max1dayDuring1month);
        assertTrue(max1minDuring1day <= max1hourDuring1week);
        assertTrue(max1hourDuring1week <= max1dayDuring1month);
    }

}