package fr.flolec.alpacabot.schedulers;

import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.clockinfo.ClockInfoModel;
import fr.flolec.alpacabot.alpacaapi.clockinfo.ClockInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class MainScheduler {

    private final ClockInfoService clockInfoService;
    private ClockInfoModel clockInfoModel;
    private final List<String> symbols = List.of("AAL");

    public MainScheduler(ClockInfoService clockInfoService) {
        this.clockInfoService = clockInfoService;
    }

    @Scheduled(cron = "0/10 * * * *")
    public void checkMarketClockBeforeUpdate() throws AlpacaApiException {
        if (clockInfoModel.isOpen()) {
            if (new Date().before(clockInfoModel.getNextClose())) {
                updateBarsAndAnalyseMarket();
            } else {
                log.info("Market is closing");
                clockInfoModel = clockInfoService.getClockInfo();
            }
        } else if (new Date().after(clockInfoModel.getNextOpen())) {
            log.info("Market is opening");
            clockInfoModel = clockInfoService.getClockInfo();
            updateBarsAndAnalyseMarket();
        }
    }

    private void updateBarsAndAnalyseMarket() {
        // Get the oldest of the newest bars in the database

    }

}
