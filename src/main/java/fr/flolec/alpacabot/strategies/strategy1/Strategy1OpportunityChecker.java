package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@Setter
public class Strategy1OpportunityChecker {

    private final Logger logger = LoggerFactory.getLogger(Strategy1Service.class);
    private AssetService assetService;
    private final BarService barService;
    private final LatestQuoteService latestQuoteService;
    private final Strategy1TicketRepository strategy1TicketRepository;
    private final Strategy1Service strategy1Service;

    @Value("${THRESHOLD}")
    private double threshold;
    @Value("${TIMEFRAME}")
    private String barTimeFrameLabel;
    @Value("${PERIOD_LENGTH}")
    private int periodLength;
    @Value("${PERIOD_LENGTH_UNIT}")
    private String periodLengthUnitLabel;
    @Value("${PREVIOUSLY_BOUGHT_PERCENTAGE}")
    private double previouslyBoughtPercentage;

    @Autowired
    public Strategy1OpportunityChecker(AssetService assetService,
                                       BarService barService,
                                       LatestQuoteService latestQuoteService,
                                       Strategy1TicketRepository strategy1TicketRepository,
                                       Strategy1Service strategy1Service) {
        this.assetService = assetService;
        this.barService = barService;
        this.latestQuoteService = latestQuoteService;
        this.strategy1TicketRepository = strategy1TicketRepository;
        this.strategy1Service = strategy1Service;
    }

    @Scheduled(cron = "${STRATEGY_1_CRON}")
    public void checkBuyOpportunities() throws IOException {
        logger.info("[STRATEGY 1] [BUY OPPORTUNITIES]");
        List<AssetModel> assets = assetService.getAssetsList(); // On commence par prendre la liste de tous les assets existants
        assets = removeAssetsUnderThreshold(assets);            // On retire les assets dont le prix ne représente pas une opportunité
        assets = removeAssetsAlreadyBought(assets);             // On retire les assets ayant un ordre d'achat filled mais pas encore revendu (pas de dual sellOrder) dont le prix est trop proche du cours actuel
        assets.forEach(strategy1Service::createBuyOrder);
    }

    public List<AssetModel> removeAssetsUnderThreshold(List<AssetModel> assets) {
        List<AssetModel> filteredAssets = new ArrayList<>();
        assets.forEach(asset -> {
            logger.info("Is {}'s price interesting?", asset.getName().split(" /")[0].trim());
            try {
                asset.setLatestValue(latestQuoteService.getLatestQuote(asset));
                double maxHigh = barService.getMaxHighOnPeriod(asset, barTimeFrameLabel, periodLength, periodLengthUnitLabel);
                maxHigh = Math.max(maxHigh, asset.getLatestValue());
                if (decreasedMoreThanThreshold(asset, maxHigh)) {
                    filteredAssets.add(asset);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        logger.info("Number of opportunities: {}/{}", filteredAssets.size(), assets.size());
        return filteredAssets;
    }

    public boolean decreasedMoreThanThreshold(AssetModel asset, double maxHigh) {
        if (asset.getLatestValue() > maxHigh) {
            throw new RuntimeException("Asset latest value should be above its maxHigh");
        }
        double decreasePercent = ((maxHigh - asset.getLatestValue()) / maxHigh) * 100;
        logAssetThresholdState(asset, decreasePercent, maxHigh);
        return decreasePercent >= threshold;
    }

    public List<AssetModel> removeAssetsAlreadyBought(List<AssetModel> assets) {
        List<AssetModel> filteredAssets = new ArrayList<>();
        assets.forEach(asset -> {
            logger.info("Do we already have uncompleted {} tickets with similar buying price?", asset.getName().split(" /")[0].trim());
            checkAssetUncompletedTickets(asset, filteredAssets);
        });
        logger.info("Number of opportunities: {}/{}", filteredAssets.size(), assets.size());
        return filteredAssets;
    }

    private void checkAssetUncompletedTickets(AssetModel asset, List<AssetModel> filteredAssets) {
        // pour chaque asset, récupérer la liste des uncompleted tickets de cet asset
        List<Strategy1TicketModel> tickets = strategy1TicketRepository.findUncompletedTickets(asset.getSymbol());
        // si la liste est vide : is ok
        if (tickets.isEmpty()) {
            logger.info("\uD83D\uDFE2 No uncompleted {} tickets in database", asset.getSymbol());
            filteredAssets.add(asset);
        } else {
            // sinon : on calcule la valeur minimale
            Double minBuyPrice = minBuyPriceFromTicketList(tickets);
            // si la valeur minimum d'achat est plus grande que (valeur actuelle + delta) :  is ok
            if (minBuyPrice > (1 + (previouslyBoughtPercentage / 100)) * asset.getLatestValue()) {
                logger.info("\uD83D\uDFE2 Min value of {} tickets in DB is high enough ({}) compared to current value ({})", asset.getSymbol(), minBuyPrice, asset.getLatestValue());
                filteredAssets.add(asset);
            } else {
                logger.info("\uD83D\uDD34 Min value of {} tickets in DB is NOT high enough ({}) compared to current value ({})", asset.getSymbol(), minBuyPrice, asset.getLatestValue());
            }
        }
    }

    public Double minBuyPriceFromTicketList(List<Strategy1TicketModel> tickets) {
        return tickets.stream()
                .map(Strategy1TicketModel::getAverageFilledBuyPrice)
                .min(Double::compare)
                .orElse(-1.);
    }

    private void logAssetThresholdState(AssetModel asset, double decreasePercent, double maxHigh) {
        String decreasedPercentString = BigDecimal.valueOf(decreasePercent)
                .setScale(2, RoundingMode.HALF_DOWN)
                .toString();
        String state;
        if (decreasePercent >= threshold) {
            state = "\uD83D\uDFE2";
        } else {
            state = "\uD83D\uDD34";
        }
        logger.info("[{}\uD83D\uDCC9 {}%] {} ({}): [Highest: ${}] [Latest: {}$]",
                state,
                decreasedPercentString,
                asset.getName().split(" /")[0].trim(),
                asset.getSymbol(),
                maxHigh,
                asset.getLatestValue());
    }
}
