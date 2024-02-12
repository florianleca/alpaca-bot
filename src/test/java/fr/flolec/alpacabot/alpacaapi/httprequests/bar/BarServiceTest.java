package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AlpacaBotApplication.class)
class BarServiceTest {

    private final Logger logger = LoggerFactory.getLogger(BarServiceTest.class);

    private final BarService barService;

    private final AssetModel unAsset;

    @Autowired
    public BarServiceTest(BarService barService, AssetService assetService) throws IOException {
        this.barService = barService;
        unAsset = assetService.getAssetsList().get(0);
    }

    @Test
    @DisplayName("BarModel attributes are correct")
    void checkBarModel() throws IOException {
        List<BarModel> bars = barService.getHistoricalBars(unAsset, BarTimeFrame.DAY1, 24, PeriodLengthUnit.HOUR);
        BarModel bar = bars.get(0);
        assertThat(bar.getDate()).isBeforeOrEqualTo(new Date());
        assertTrue(bar.getHigh() >= bar.getOpen());
        assertTrue(bar.getHigh() >= bar.getClose());
        assertTrue(bar.getLow() <= bar.getOpen());
        assertTrue(bar.getLow() <= bar.getClose());
    }

    @Test
    @DisplayName("Wrong ENUM values should raise exceptions")
    void wrongLabels() {
        assertThrows(IllegalArgumentException.class, () -> BarTimeFrame.fromLabel("wrong label"));
        assertThrows(IllegalArgumentException.class, () -> PeriodLengthUnit.fromLabel("wrong label"));

    }

    @Test
    @DisplayName("1 daily bar in the last 24 hours")
    void getHistoricalBars1Daily24Hours() throws IOException {
        List<BarModel> bars = barService.getHistoricalBars(unAsset, BarTimeFrame.DAY1, 24, PeriodLengthUnit.HOUR);
        logger.info("{} daily bars were retrieved over the last 24h.", bars.size());
        assertEquals(1, bars.size());
    }

    @Test
    @DisplayName("7 daily bar in the last week")
    void getHistoricalBars7Daily1Week() throws IOException {
        List<BarModel> bars = barService.getHistoricalBars(unAsset, BarTimeFrame.DAY1, 1, PeriodLengthUnit.WEEK);
        logger.info("{} daily bars were retrieved over the last week", bars.size());
        assertEquals(7, bars.size());
    }

    @Test
    @DisplayName("Local max locals lower than global max")
    void getMaxHighOnPeriod() throws IOException {
        double max1minDuring1day = barService.getMaxHighOnPeriod(unAsset, "1Min", 1, "Day");
        double max1hourDuring1week = barService.getMaxHighOnPeriod(unAsset, BarTimeFrame.HOUR1, 1, PeriodLengthUnit.WEEK);
        double max1dayDuring1month = barService.getMaxHighOnPeriod(unAsset, BarTimeFrame.DAY1, 1, PeriodLengthUnit.MONTH);
        logger.info("Maximum values of {} over the last...", unAsset.getSymbol());
        logger.info("...day: ${}", max1minDuring1day);
        logger.info("...week: ${}", max1hourDuring1week);
        logger.info("...month: ${}", max1dayDuring1month);
        assertTrue(max1minDuring1day <= max1hourDuring1week);
        assertTrue(max1hourDuring1week <= max1dayDuring1month);
    }

}