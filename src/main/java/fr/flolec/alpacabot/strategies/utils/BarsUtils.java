package fr.flolec.alpacabot.strategies.utils;

import fr.flolec.alpacabot.alpacaapi.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.bar.BarTimeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BarsUtils {

    private static final Logger logger = LoggerFactory.getLogger(BarsUtils.class);

    // Private constructor to prevent instantiation
    private BarsUtils() {
    }

    public static BarSeries barModelListToBarSeries(List<BarModel> rawBars, BarTimeFrame barTimeFrame) {
        BarSeries barSeries = new BaseBarSeries();
        rawBars = BarsUtils.sortBarsList(rawBars);
        rawBars = BarsUtils.removeDuplicatesFromBarsList(rawBars);
        rawBars.forEach(bar -> {
            ZonedDateTime barBeginTime = ZonedDateTime.parse(bar.getBeginTime());
            ZonedDateTime barEndTime = barBeginTime.plus(barTimeFrame.getTemporalAmount());
            barSeries.addBar(barEndTime, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume());
        });
        return barSeries;
    }

    public static void barSeriesToCsvFile(BarSeries series, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Date,Open,High,Low,Close,Volume\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

            for (Bar bar : series.getBarData()) {
                writer.append(bar.getEndTime().format(formatter)).append(",");
                writer.append(bar.getOpenPrice().toString()).append(",");
                writer.append(bar.getHighPrice().toString()).append(",");
                writer.append(bar.getLowPrice().toString()).append(",");
                writer.append(bar.getClosePrice().toString()).append(",");
                writer.append(bar.getVolume().toString()).append("\n");
            }
        }
    }

    public static BarSeries csvFileToBarSeries(String filePath, Duration duration) throws IOException {
        BarSeries series = new BaseBarSeries();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        reader.lines().forEachOrdered(line -> {
            String[] values = line.split(",");
            double open = Double.parseDouble(values[1]);
            double high = Double.parseDouble(values[2]);
            double low = Double.parseDouble(values[3]);
            double close = Double.parseDouble(values[4]);
            double volume = Double.parseDouble(values[5]);
            ZonedDateTime endTime = Instant.ofEpochMilli(Long.parseLong(values[6]) + 1)
                    .atZone(ZoneId.of("UTC"));
            series.addBar(new BaseBar(duration, endTime, open, high, low, close, volume));
        });
        reader.close();
        return series;
    }

    public static List<BarModel> sortBarsList(List<BarModel> rawBars) {
        return rawBars.stream()
                .sorted(Comparator.comparing(bar -> ZonedDateTime.parse(bar.getBeginTime())))
                .toList();
    }

    public static List<BarModel> removeDuplicatesFromBarsList(List<BarModel> rawBars) {
        if (rawBars.size() <= 1) {
            return rawBars;
        }
        int count = 0;
        List<BarModel> bars = new ArrayList<>();
        for (int i = 0; i < rawBars.size() - 1; i++) {
            BarModel currentBar = rawBars.get(i);
            BarModel nextBar = rawBars.get(i + 1);
            if (!currentBar.getBeginTime().equals(nextBar.getBeginTime())) {
                bars.add(currentBar);
            } else {
                count++;
            }
        }
        bars.add(rawBars.get(rawBars.size() - 1));
        logger.info("Removed {} duplicate(s) from bars list.", count);
        return bars;
    }

}
