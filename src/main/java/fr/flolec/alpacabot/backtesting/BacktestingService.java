package fr.flolec.alpacabot.backtesting;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.strategies.StrategyBuilder;
import fr.flolec.alpacabot.strategies.StrategyEnum;
import fr.flolec.alpacabot.strategies.utils.BarsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;

import java.io.IOException;
import java.util.List;

@Service
public class BacktestingService {

    private final ApplicationContext applicationContext;

    private final BarService barService;


    @Autowired
    public BacktestingService(ApplicationContext applicationContext,
                              BarService barService) {
        this.applicationContext = applicationContext;
        this.barService = barService;
    }

    public BacktestResult backtesting(StrategyEnum strategyEnum,
                              String symbol,
                              int periodLength,
                              PeriodLengthUnit periodLengthUnit,
                              BarTimeFrame barTimeFrame) throws IOException {

        // Get historical bars
        List<BarModel> rawBars = barService.getHistoricalBars(symbol, barTimeFrame, periodLength, periodLengthUnit);
        BarSeries series = BarsUtils.barModelListToBarSeries(rawBars);

        // Build strategy
        StrategyBuilder bean = (StrategyBuilder) applicationContext.getBean(strategyEnum.getClazz());
        Strategy strategy = bean.buildStrategy(series);

        // Run back-test
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return new BacktestResult(series, tradingRecord);
    }

}
