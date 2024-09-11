package fr.flolec.alpacabot.strategies.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class ChartBuilder {

    /**
     * @param barSeries The bar series
     * @param indicator The indicator to be added to the chart
     * @param name      The name of displayed indicator
     * @return          A time series containing the values of the indicator
     */
    private static TimeSeries buildChartTimeSeries(BarSeries barSeries, Indicator<Num> indicator,
                                                   String name) {
        TimeSeries chartTimeSeries = new TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            Bar bar = barSeries.getBar(i);
            chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())),
                    indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }

    private static void addBuySellSignals(BarSeries series, Strategy strategy, XYPlot plot) {
        // Running the strategy
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        List<Position> positions = seriesManager.run(strategy).getPositions();
        // Adding markers to plot
        for (Position position : positions) {
            // Buy signal
            double buySignalBarTime = new Minute(
                    Date.from(series.getBar(position.getEntry().getIndex()).getEndTime().toInstant()))
                    .getFirstMillisecond();
            Marker buyMarker = new ValueMarker(buySignalBarTime);
            buyMarker.setPaint(Color.GREEN);
            buyMarker.setLabel("B");
            plot.addDomainMarker(buyMarker);
            // Sell signal
            double sellSignalBarTime = new Minute(
                    Date.from(series.getBar(position.getExit().getIndex()).getEndTime().toInstant()))
                    .getFirstMillisecond();
            Marker sellMarker = new ValueMarker(sellSignalBarTime);
            sellMarker.setPaint(Color.RED);
            sellMarker.setLabel("S");
            plot.addDomainMarker(sellMarker);
        }
    }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(1024, 400));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Strategy preview on chart");
        frame.setContentPane(panel);
        frame.pack();
        UIUtils.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    /**
     * System.setProperty("java.awt.headless", "false");
     * BarSeries bars = barsUtils.getLastHourBars("BTC/USD", BarTimeFrame.HOUR1, 1, PeriodLengthUnit.WEEK);
     * chartBuilder.run(strategy2Service.buildStrategy2(bars), bars, "BTC/USD");
     * Thread.sleep(100000);
     */
    public void run(Strategy strategy, BarSeries series, String assetSymbol) {

        //Building chart datasets
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), assetSymbol + " close values"));

        //Creating the chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                assetSymbol, // main title
                "Date", // x-axis label
                "Price", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

        //Running the strategy and adding the buy and sell signals to plot
        addBuySellSignals(series, strategy, plot);

        // Displaying the chart
        displayChart(chart);
    }

}
