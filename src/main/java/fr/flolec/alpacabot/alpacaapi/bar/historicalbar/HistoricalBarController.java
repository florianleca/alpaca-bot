package fr.flolec.alpacabot.alpacaapi.bar.historicalbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/historical-bars")
public class HistoricalBarController {

    private final HistoricalBarService historicalBarService;

    public HistoricalBarController(HistoricalBarService historicalBarService) {
        this.historicalBarService = historicalBarService;
    }

    @GetMapping("load-db")
    public List<String> loadHistoricalBars(@RequestParam String assetSymbol,
                                           @RequestParam int numberOfBars) throws AlpacaApiException, JsonProcessingException {
        return historicalBarService.loadHistoricalBars(assetSymbol, numberOfBars);
    }

    @DeleteMapping("empty-db")
    public void emptyHistoricalBars() {
        historicalBarService.deleteAll();
    }

}
