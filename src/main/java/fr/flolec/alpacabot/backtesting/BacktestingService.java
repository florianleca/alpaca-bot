package fr.flolec.alpacabot.backtesting;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.historicalbar.HistoricalBarService;
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

import java.util.List;

@Service
public class BacktestingService {

    private final ApplicationContext applicationContext;

    private final HistoricalBarService historicalBarService;


    @Autowired
    public BacktestingService(ApplicationContext applicationContext,
                              HistoricalBarService historicalBarService) {
        this.applicationContext = applicationContext;
        this.historicalBarService = historicalBarService;
    }

    public BacktestResult backtesting(StrategyEnum strategyEnum,
                                      String symbol,
                                      int periodLength,
                                      PeriodLengthUnit periodLengthUnit,
                                      BarTimeFrame barTimeFrame,
                                      boolean isCrypto) {

        // Get historical bars
        List<BarModel> rawBars = historicalBarService.getHistoricalBars(symbol, barTimeFrame, periodLength, periodLengthUnit, isCrypto);
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
