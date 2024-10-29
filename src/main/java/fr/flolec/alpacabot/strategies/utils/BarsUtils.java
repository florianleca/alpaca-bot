package fr.flolec.alpacabot.strategies.utils;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BarsUtils {

    private BarsUtils() {
    }

    public static BarSeries barModelListToBarSeries(List<BarModel> rawBars) {
        BarSeries barSeries = new BaseBarSeries();
        rawBars.forEach(bar -> {
            ZonedDateTime barDate = ZonedDateTime.parse(bar.getDate());
            if (barSeries.getBarCount() == 0 || barDate.isAfter(barSeries.getLastBar().getEndTime())) {
                barSeries.addBar(ZonedDateTime.parse(bar.getDate()), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume());
            }
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

}
