package fr.flolec.alpacabot.strategies.utils;

import fr.flolec.alpacabot.alpacaapi.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.bar.BarTimeFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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

class BarsUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void barModelListToBarSeries_nominal_mappingOk() {
        BarModel barModel1 = new BarModel("2021-01-01T00:00:00Z", 1.0, 2.0, 0.5, 1.5, 1000.0);
        BarModel barModel2 = new BarModel("2021-01-01T01:00:00Z", 1.1, 2.1, 0.6, 1.6, 1100.0);
        List<BarModel> rawBars = List.of(barModel1, barModel2);

        BarSeries barSeries = BarsUtils.barModelListToBarSeries(rawBars, BarTimeFrame.HOUR1);

        assertEquals(2, barSeries.getBarCount());
        assertEquals(1.0, barSeries.getBar(0).getClosePrice().doubleValue());
        assertEquals(2.0, barSeries.getBar(0).getHighPrice().doubleValue());
        assertEquals(0.5, barSeries.getBar(0).getLowPrice().doubleValue());
        assertEquals(1.5, barSeries.getBar(0).getOpenPrice().doubleValue());
        assertEquals(1000.0, barSeries.getBar(0).getVolume().doubleValue());
    }

    @Test
    void barSeriesToCsvFile_nominal_csvFileWritten() throws IOException {
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
    void csvFileToBarSeries_nominal_mappingOk() throws IOException {
        BarSeries barSeries = BarsUtils.csvFileToBarSeries("src/test/resources/BTCUSDT-1m-2024-09-21.csv", Duration.ofMinutes(1));

        assertEquals(1440, barSeries.getBarCount());

        Bar firstBar = barSeries.getFirstBar();
        assertEquals(63201.05000000, firstBar.getOpenPrice().doubleValue());
        assertEquals(63201.06000000, firstBar.getHighPrice().doubleValue());
        assertEquals(63186.00000000, firstBar.getLowPrice().doubleValue());
        assertEquals(63196.00000000, firstBar.getClosePrice().doubleValue());
        assertEquals(10.91101000, firstBar.getVolume().doubleValue());
        // 1726876800000L = 2024-09-21T00:00:00Z
        assertEquals(Instant.ofEpochMilli(1726876800000L), firstBar.getBeginTime().toInstant());
        // 1726876860000L = 2024-09-21T00:01:00Z
        assertEquals(Instant.ofEpochMilli(1726876860000L), firstBar.getEndTime().toInstant());

        Bar lastBar = barSeries.getLastBar();
        // 1726963140000L = 2024-09-21T23:59:00Z
        assertEquals(Instant.ofEpochMilli(1726963140000L), lastBar.getBeginTime().toInstant());
        // 1726963200000L = 2024-09-22T00:00:00Z
        assertEquals(Instant.ofEpochMilli(1726963200000L), lastBar.getEndTime().toInstant());
    }

    @Test
    void sortBarsList_nominal_sortedList() {
        BarModel barModel1 = new BarModel("2021-01-01T01:00:00Z", 1.1, 2.1, 0.6, 1.6, 1100.0);
        BarModel barModel2 = new BarModel("2021-01-01T04:00:00Z", 1.0, 2.0, 0.5, 1.5, 1000.0);
        BarModel barModel3 = new BarModel("2021-01-01T03:00:00Z", 1.1, 2.1, 0.6, 1.6, 1100.0);
        BarModel barModel4 = new BarModel("2021-01-01T06:00:00Z", 1.0, 2.0, 0.5, 1.5, 1000.0);
        BarModel barModel5 = new BarModel("2021-01-01T02:00:00Z", 1.1, 2.1, 0.6, 1.6, 1100.0);
        BarModel barModel6 = new BarModel("2021-01-01T05:00:00Z", 1.0, 2.0, 0.5, 1.5, 1000.0);
        List<BarModel> rawBars = List.of(barModel1, barModel2, barModel3, barModel4, barModel5, barModel6);

        List<BarModel> sortedBars = BarsUtils.sortBarsList(rawBars);

        assertEquals(Instant.parse("2021-01-01T01:00:00Z"), sortedBars.get(0).getBeginTime());
        assertEquals(Instant.parse("2021-01-01T02:00:00Z"), sortedBars.get(1).getBeginTime());
        assertEquals(Instant.parse("2021-01-01T04:00:00Z"), sortedBars.get(3).getBeginTime());
        assertEquals(Instant.parse("2021-01-01T05:00:00Z"), sortedBars.get(4).getBeginTime());
        assertEquals(Instant.parse("2021-01-01T03:00:00Z"), sortedBars.get(2).getBeginTime());
        assertEquals(Instant.parse("2021-01-01T06:00:00Z"), sortedBars.get(5).getBeginTime());
    }

    @Test
    void removeDuplicatesFromBarsList_nominal_duplicatesRemoved() {
        BarModel barModel1 = new BarModel("2021-01-01T01:00:00Z", 1.1, 2.1, 0.1, 1.1, 1100.0);
        BarModel barModel2 = new BarModel("2021-01-01T01:00:00Z", 1.2, 2.2, 0.2, 1.2, 1200.0);
        BarModel barModel3 = new BarModel("2021-01-01T03:00:00Z", 1.3, 2.3, 0.3, 1.3, 1300.0);
        BarModel barModel4 = new BarModel("2021-01-01T03:00:00Z", 1.4, 2.4, 0.4, 1.4, 1400.0);
        BarModel barModel5 = new BarModel("2021-01-01T03:00:00Z", 1.5, 2.5, 0.5, 1.5, 1500.0);
        List<BarModel> rawBars = List.of(barModel1, barModel2, barModel3, barModel4, barModel5);

        List<BarModel> bars = BarsUtils.removeDuplicatesFromBarsList(rawBars);

        assertEquals(2, bars.size());
        assertEquals(Instant.parse("2021-01-01T01:00:00Z"), bars.get(0).getBeginTime());
        assertEquals(Instant.parse("2021-01-01T03:00:00Z"), bars.get(1).getBeginTime());
    }

    @Test
    void removeDuplicatesFromBarsList_oneElement_noChange() {
        BarModel barModel = new BarModel("2021-01-01T01:00:00Z", 1.1, 2.1, 0.1, 1.1, 1100.0);
        List<BarModel> rawBars = List.of(barModel);

        List<BarModel> bars = BarsUtils.removeDuplicatesFromBarsList(rawBars);

        assertEquals(1, bars.size());
        assertEquals(barModel, bars.get(0));
    }

    /*
    CSV Binance :
    OPEN_TIME / open / high / low / close / volume / CLOSE_TIME / ...

    1ère ligne :
    OPEN :  Thursday 1 August 2024 00:00:00
    CLOSE : Thursday 1 August 2024 01:00:00

    public BaseBar(
            Duration timePeriod,
        --> ZonedDateTime endTime,
            double openPrice,
            double highPrice,
            double lowPrice,
            double closePrice,
            double volume
    )
     */

}