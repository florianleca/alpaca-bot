package fr.flolec.alpacabot.backtesting;

import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.strategies.StrategyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/backtesting")
public class BacktestingController {

    private final BacktestingService backtestingService;

    @Autowired
    public BacktestingController(BacktestingService backtestingService) {
        this.backtestingService = backtestingService;
    }

    @GetMapping("{strategy}")
    public BacktestResult backtesting(@PathVariable("strategy") StrategyEnum strategy,
                              @RequestParam("symbol") String symbol,
                              @RequestParam("periodLength") int periodLength,
                              @RequestParam("periodLengthUnit") PeriodLengthUnit periodLengthUnit,
                              @RequestParam("timeFrame") BarTimeFrame barTimeFrame) throws IOException {
        return backtestingService.backtesting(strategy, symbol, periodLength, periodLengthUnit, barTimeFrame);
    }

}
