package fr.flolec.alpacabot.strategies.strategy2;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class Strategy2Service {

    @Autowired
    private BarService barService;

    public BarSeries getLastHourBars(String asset) throws IOException {
        List<BarModel> rawBars = barService.getHistoricalBars(asset, BarTimeFrame.HOUR1, 1, PeriodLengthUnit.MONTH);
        BarSeries barSeries = new BaseBarSeries();
        rawBars.forEach(bar -> barSeries.addBar(ZonedDateTime.parse(bar.getDate()), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose()));
        return barSeries;
    }

    public Strategy buildStrategy2(BarSeries series) {
        // Indicators
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macdIndicator = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator macdSignalIndicator = new EMAIndicator(macdIndicator, 9);
        // Rules
        Rule buyingRule = new CrossedUpIndicatorRule(macdIndicator, macdSignalIndicator)
                .and(new OverIndicatorRule(macdIndicator, 0));
        Rule sellingRule = new CrossedDownIndicatorRule(macdIndicator, macdSignalIndicator);
        // Strategy
        return new BaseStrategy(buyingRule, sellingRule);
    }

}
