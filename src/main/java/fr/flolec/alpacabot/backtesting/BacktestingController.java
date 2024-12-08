package fr.flolec.alpacabot.backtesting;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.strategies.StrategyEnum;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backtesting")
public class BacktestingController {

    private final BacktestingService backtestingService;

    public BacktestingController(BacktestingService backtestingService) {
        this.backtestingService = backtestingService;
    }

    @GetMapping("{strategy}")
    public BacktestResult backtesting(@PathVariable("strategy") StrategyEnum strategy,
                                      @RequestParam("symbol") String symbol,
                                      @RequestParam("periodLength") int periodLength,
                                      @RequestParam("periodLengthUnit") PeriodLengthUnit periodLengthUnit,
                                      @RequestParam("timeFrame") BarTimeFrame barTimeFrame,
                                      @RequestParam("isCrypto") boolean isCrypto) throws AlpacaApiException, JsonProcessingException {
        return backtestingService.backtesting(strategy, symbol, periodLength, periodLengthUnit, barTimeFrame, isCrypto);
    }

}
