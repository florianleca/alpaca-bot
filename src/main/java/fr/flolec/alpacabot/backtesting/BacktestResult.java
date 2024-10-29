package fr.flolec.alpacabot.backtesting;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.ReturnCriterion;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class BacktestResult {

    @JsonProperty
    private final double roi;

    @JsonProperty
    private final List<PositionSummary> positions;


    public BacktestResult(BarSeries series, TradingRecord tradingRecord) {
        this.roi = new ReturnCriterion().calculate(series, tradingRecord).doubleValue();
        this.positions = tradingRecord.getPositions().stream()
                .map(position -> new PositionSummary(series, position))
                .toList();
    }

    @Getter
    public static class PositionSummary {

        /**
         * Begin time of the entry bar
         */
        @JsonProperty
        private final ZonedDateTime entryDate;

        @JsonProperty
        private final double entryPrice;

        /**
         * Begin time of the exit bar
         */
        @JsonProperty
        private final ZonedDateTime exitDate;

        @JsonProperty
        private final double exitPrice;

        @JsonProperty
        private final double profit;

        public PositionSummary(BarSeries series, Position position) {
            int entryIndex = position.getEntry().getIndex();
            this.entryDate = series.getBar(entryIndex).getBeginTime();
            int exitIndex = position.getExit().getIndex();
            this.exitDate = series.getBar(exitIndex).getBeginTime();
            this.entryPrice = position.getEntry().getPricePerAsset().doubleValue();
            this.exitPrice = position.getExit().getPricePerAsset().doubleValue();
            this.profit = position.getProfit().doubleValue();
        }
    }

}
