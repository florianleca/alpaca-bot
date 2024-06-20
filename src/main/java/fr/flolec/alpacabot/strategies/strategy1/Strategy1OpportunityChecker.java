package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@Setter
public class Strategy1OpportunityChecker {

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

    private final BarService barService;
    private final LatestQuoteService latestQuoteService;
    private final Strategy1TicketRepository strategy1TicketRepository;
    private Logger logger = LoggerFactory.getLogger(Strategy1Service.class);
    private AssetService assetService;

    @Autowired
    public Strategy1OpportunityChecker(AssetService assetService,
                                       BarService barService,
                                       LatestQuoteService latestQuoteService,
                                       Strategy1TicketRepository strategy1TicketRepository) {
        this.assetService = assetService;
        this.barService = barService;
        this.latestQuoteService = latestQuoteService;
        this.strategy1TicketRepository = strategy1TicketRepository;
    }

    public List<AssetModel> checkBuyOpportunities() throws IOException {
        logger.info("[STRATEGY 1] [BUY OPPORTUNITIES]");
        List<AssetModel> assets = assetService.getAssetsList(); // On commence par prendre la liste de tous les assets existants
        assets = removeAssetsUnderThreshold(assets);            // On retire les assets dont le prix ne représente pas une opportunité
        return removeAssetsAlreadyBought(assets);             // On retire les assets ayant un ordre d'achat filled mais pas encore revendu (pas de dual sellOrder) dont le prix est trop proche du cours actuel
    }

    public List<AssetModel> removeAssetsUnderThreshold(List<AssetModel> assets) {
        logger.info("[STRATEGY 1] [REMOVE OPPORTUNITIES UNDER THRESHOLD]");
        List<AssetModel> filteredAssets = new ArrayList<>();
        assets.forEach(asset -> {
            if (isAssetPriceLowEnough(asset)) filteredAssets.add(asset);
        });
        logger.info("Number of opportunities: {}/{}", filteredAssets.size(), assets.size());
        return filteredAssets;
    }

    public boolean isAssetPriceLowEnough(AssetModel asset) {
        try {
            double assetLatestValue = latestQuoteService.getLatestQuote(asset);
            asset.setLatestValue(assetLatestValue);
            double maxHigh = barService.getMaxHighOnPeriod(asset, BarTimeFrame.fromLabel(barTimeFrameLabel), periodLength, PeriodLengthUnit.fromLabel(periodLengthUnitLabel));
            maxHigh = Math.max(maxHigh, assetLatestValue);
            if (decreasedMoreThanThreshold(asset, assetLatestValue, maxHigh)) {
                return true;
            }
        } catch (IOException e) {
            logger.error("Error while fetching latest quote or max high for {}: {}", asset.getSymbol(), e.getMessage());
        }
        return false;
    }


    public boolean decreasedMoreThanThreshold(AssetModel asset, double assetLatestValue, double maxHigh) {
        if (maxHigh == 0) {
            logger.error("Max high of {} is 0, which should not be possible", asset.getSymbol());
            return false;
        }
        if (assetLatestValue > maxHigh) {
            logger.error("Latest value of {} is higher than its max high, which should not be possible", asset.getSymbol());
            return false;
        }
        double decreasePercent = (((maxHigh - assetLatestValue) / maxHigh) * 100);
        logAssetThresholdState(asset, decreasePercent, maxHigh);
        return decreasePercent >= threshold;
    }

    public void logAssetThresholdState(AssetModel asset, double decreasePercent, double maxHigh) {
        String decreasedPercentString = BigDecimal.valueOf(decreasePercent)
                .setScale(2, RoundingMode.HALF_DOWN)
                .toString();
        String state;
        if (decreasePercent >= threshold) {
            state = "🟢";
        } else {
            state = "🔴";
        }
        logger.info("[{}📉 {}%] {}: [Highest: ${}] [Latest: {}$]",
                state,
                decreasedPercentString,
                asset.getSymbol(),
                maxHigh,
                asset.getLatestValue());
    }

    public List<AssetModel> removeAssetsAlreadyBought(List<AssetModel> assets) {
        logger.info("[STRATEGY 1] [REMOVE ALREADY BOUGHT OPPORTUNITIES]");
        List<AssetModel> filteredAssets = new ArrayList<>();
        assets.forEach(asset -> {
            if (checkAssetUncompletedTickets(asset)) filteredAssets.add(asset);
        });
        logger.info("Number of opportunities: {}/{}", filteredAssets.size(), assets.size());
        return filteredAssets;
    }

    public boolean checkAssetUncompletedTickets(AssetModel asset) {
        // pour chaque asset, récupérer la liste des uncompleted tickets de cet asset
        List<Strategy1TicketModel> tickets = strategy1TicketRepository.findUncompletedTickets(asset.getSymbol());
        // si la liste est vide : is ok
        if (tickets.isEmpty()) {
            logger.info("🟢 No uncompleted {} tickets in database", asset.getSymbol());
            return true;
        }
        // sinon : on calcule la valeur minimale
        Double minBuyPrice = minBuyPriceFromTicketList(tickets);
        // si la valeur minimum d'achat est plus grande que (valeur actuelle + delta) :  is ok
        if (minBuyPrice > (1 + (previouslyBoughtPercentage / 100)) * asset.getLatestValue()) {
            logger.info("🟢 Min value of {} tickets in DB is high enough ({}) compared to current value ({})", asset.getSymbol(), minBuyPrice, asset.getLatestValue());
            return true;
        }
        logger.info("🔴 Min value of {} tickets in DB is NOT high enough ({}) compared to current value ({})", asset.getSymbol(), minBuyPrice, asset.getLatestValue());
        return false;
    }

    public Double minBuyPriceFromTicketList(List<Strategy1TicketModel> tickets) {
        return tickets.stream()
                .map(Strategy1TicketModel::getAverageFilledBuyPrice)
                .min(Double::compare)
                .orElse(-1.);
    }

}
