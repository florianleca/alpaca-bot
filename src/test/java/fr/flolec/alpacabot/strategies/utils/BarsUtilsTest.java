package fr.flolec.alpacabot.strategies.utils;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarsUtilsTest {

    @TempDir
    Path tempDir;
    @Mock
    private BarService barService;
    @Spy
    @InjectMocks
    private BarsUtils barsUtils;

    @Test
    void getLastHourBars() throws IOException {
        BarModel barModel1 = new BarModel("2021-01-01T00:00:00Z", 1.0, 2.0, 0.5, 1.5, 1000.0);
        BarModel barModel2 = new BarModel("2021-01-01T01:00:00Z", 1.1, 2.1, 0.6, 1.6, 1100.0);

        List<BarModel> rawBars = List.of(barModel1, barModel2);

        when(barService.getHistoricalBars("BTC/USD", BarTimeFrame.HOUR1, 1, PeriodLengthUnit.WEEK)).thenReturn(rawBars);

        BarSeries barSeries = barsUtils.getLastHourBars("BTC/USD", BarTimeFrame.HOUR1, 1, PeriodLengthUnit.WEEK);

        verify(barService, times(1)).getHistoricalBars("BTC/USD", BarTimeFrame.HOUR1, 1, PeriodLengthUnit.WEEK);
        assertEquals(2, barSeries.getBarCount());
        assertEquals(1.0, barSeries.getBar(0).getClosePrice().doubleValue());
        assertEquals(2.0, barSeries.getBar(0).getHighPrice().doubleValue());
        assertEquals(0.5, barSeries.getBar(0).getLowPrice().doubleValue());
        assertEquals(1.5, barSeries.getBar(0).getOpenPrice().doubleValue());
        assertEquals(1000.0, barSeries.getBar(0).getVolume().doubleValue());
    }

    @Test
    void barSeriesToCsvFile() throws IOException {
        Bar bar1 = new BaseBar(Duration.ofHours(1), ZonedDateTime.parse("2021-01-01T00:00:00Z"), 1.0, 2.0, 0.5, 1.5, 1000.0);
        Bar bar2 = new BaseBar(Duration.ofHours(1), ZonedDateTime.parse("2021-01-01T01:00:00Z"), 1.1, 2.1, 0.6, 1.6, 1100.0);
        BarSeries barSeries = new BaseBarSeries(List.of(bar1, bar2));

        Path csvFile = tempDir.resolve("output.csv");
        BarsUtils.barSeriesToCsvFile(barSeries, csvFile.toString());

        String expectedContent = """
                Date,Open,High,Low,Close,Volume
                2021-01-01T00:00:00+0000,1.0,2.0,0.5,1.5,1000.0
                2021-01-01T01:00:00+0000,1.1,2.1,0.6,1.6,1100.0
                """;

        String actualContent = Files.readString(csvFile);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void csvFileToBarSeries() throws IOException {
        BarSeries barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1m-2024-09-21.csv", Duration.ofMinutes(1));
        assertEquals(1440, barSeries.getBarCount());

        Bar firstBar = barSeries.getFirstBar();
        Bar lastBar = barSeries.getLastBar();

        assertEquals(63201.05000000, firstBar.getOpenPrice().doubleValue());
        assertEquals(63201.06000000, firstBar.getHighPrice().doubleValue());
        assertEquals(63186.00000000, firstBar.getLowPrice().doubleValue());
        assertEquals(63196.00000000, firstBar.getClosePrice().doubleValue());
        assertEquals(10.91101000, firstBar.getVolume().doubleValue());

        // 1726876800000L = 2024-09-21T00:00:00Z
        assertEquals(Instant.ofEpochMilli(1726876800000L), firstBar.getBeginTime().toInstant());
        // 1726876860000L = 2024-09-21T00:01:00Z
        assertEquals(Instant.ofEpochMilli(1726876860000L), firstBar.getEndTime().toInstant());

        // 1726963140000L = 2024-09-21T23:59:00Z
        assertEquals(Instant.ofEpochMilli(1726963140000L), lastBar.getBeginTime().toInstant());
        // 1726963200000L = 2024-09-22T00:00:00Z
        assertEquals(Instant.ofEpochMilli(1726963200000L), lastBar.getEndTime().toInstant());
    }

}