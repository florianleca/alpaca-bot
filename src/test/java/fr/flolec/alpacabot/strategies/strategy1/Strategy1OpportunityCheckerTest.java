package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Strategy1OpportunityCheckerTest {

    private final String barTimeFrameLabel = "1Min";
    private final int periodLength = 3;
    private final String periodLengthUnitLabel = "Hour";

    private final AssetModel asset1 = new AssetModel();
    private final AssetModel asset2 = new AssetModel();
    private final AssetModel asset3 = new AssetModel();
    private final AssetModel asset4 = new AssetModel();

    private final List<AssetModel> twoAssets = Arrays.asList(asset1, asset2);
    private final List<AssetModel> threeAssets = Arrays.asList(asset1, asset2, asset3);
    private final List<AssetModel> fourAssets = Arrays.asList(asset1, asset2, asset3, asset4);

    @Mock
    private AssetService assetService;

    @Mock
    private LatestQuoteService latestQuoteService;

    @Mock
    private BarService barService;

    @Mock
    private Strategy1TicketRepository strategy1TicketRepository;

    @Mock
    private Logger logger;

    @Spy
    @InjectMocks
    private Strategy1OpportunityChecker strategy1OpportunityChecker;

    @BeforeEach
    void setUp() {
        asset1.setSymbol("ASSET1/TEST");
        asset2.setSymbol("ASSET2/TEST");
        asset3.setSymbol("ASSET3/TEST");
        asset4.setSymbol("ASSET4/TEST");
        strategy1OpportunityChecker.setThreshold(2);
        strategy1OpportunityChecker.setPreviouslyBoughtPercentage(10);
        strategy1OpportunityChecker.setBarTimeFrameLabel(barTimeFrameLabel);
        strategy1OpportunityChecker.setPeriodLength(periodLength);
        strategy1OpportunityChecker.setPeriodLengthUnitLabel(periodLengthUnitLabel);
        strategy1OpportunityChecker.setLogger(logger);
    }

    @Test
    @DisplayName("Launching Strategy 1 opportunity checker")
    void checkBuyOpportunities() throws IOException {
        when(assetService.getAssetsList()).thenReturn(fourAssets);
        doReturn(threeAssets).when(strategy1OpportunityChecker).removeAssetsUnderThreshold(fourAssets);
        doReturn(twoAssets).when(strategy1OpportunityChecker).removeAssetsAlreadyBought(threeAssets);

        List<AssetModel> assets = strategy1OpportunityChecker.checkBuyOpportunities();

        verify(assetService, times(1)).getAssetsList();
        assertEquals(twoAssets, assets);
    }

    @Test
    @DisplayName("removeAssetsUnderThreshold")
    void removeAssetsUnderThresholdNominal() {
        doReturn(true).when(strategy1OpportunityChecker).isAssetPriceLowEnough(asset1);
        doReturn(true).when(strategy1OpportunityChecker).isAssetPriceLowEnough(asset2);
        doReturn(false).when(strategy1OpportunityChecker).isAssetPriceLowEnough(asset3);
        doReturn(false).when(strategy1OpportunityChecker).isAssetPriceLowEnough(asset4);

        List<AssetModel> assets = strategy1OpportunityChecker.removeAssetsUnderThreshold(fourAssets);

        verify(logger, times(1)).info("Number of opportunities: {}/{}", 2, 4);
        assertEquals(twoAssets, assets);
    }

    @Test
    @DisplayName("isAssetPriceLowEnough - nominal")
    void isAssetPriceLowEnough() throws IOException {
        when(latestQuoteService.getLatestQuote(asset1)).thenReturn(0.);
        when(latestQuoteService.getLatestQuote(asset2)).thenReturn(0.);

        when(barService.getMaxHighOnPeriod(asset1.getSymbol(), BarTimeFrame.fromLabel(barTimeFrameLabel), periodLength, PeriodLengthUnit.fromLabel(periodLengthUnitLabel))).thenReturn(0.);
        when(barService.getMaxHighOnPeriod(asset2.getSymbol(), BarTimeFrame.fromLabel(barTimeFrameLabel), periodLength, PeriodLengthUnit.fromLabel(periodLengthUnitLabel))).thenReturn(0.);

        doReturn(true).when(strategy1OpportunityChecker).decreasedMoreThanThreshold(asset1, 0., 0.);
        doReturn(false).when(strategy1OpportunityChecker).decreasedMoreThanThreshold(asset2, 0., 0.);

        boolean isAsset1PriceLowEnough = strategy1OpportunityChecker.isAssetPriceLowEnough(asset1);
        boolean isAsset2PriceLowEnough = strategy1OpportunityChecker.isAssetPriceLowEnough(asset2);

        assertTrue(isAsset1PriceLowEnough);
        assertFalse(isAsset2PriceLowEnough);
    }

    @Test
    @DisplayName("isAssetPriceLowEnough - error fetching data")
    void isAssetPriceLowEnoughErrorFetchingLatestQuote() throws IOException {
        when(latestQuoteService.getLatestQuote(asset1)).thenThrow(new IOException("error"));

        boolean isAsset1PriceLowEnough = strategy1OpportunityChecker.isAssetPriceLowEnough(asset1);

        assertFalse(isAsset1PriceLowEnough);
        verify(logger, times(1)).error("Error while fetching latest quote or max high for {}: {}", asset1.getSymbol(), "error");
    }

    @Test
    @DisplayName("decreasedMoreThanThreshold - nominal")
    void decreasedMoreThanThresholdNominal() {
        boolean decreasedMoreThanThreshold1 = strategy1OpportunityChecker.decreasedMoreThanThreshold(asset1, 99., 100.);
        boolean decreasedMoreThanThreshold2 = strategy1OpportunityChecker.decreasedMoreThanThreshold(asset2, 196., 200.);
        boolean decreasedMoreThanThreshold3 = strategy1OpportunityChecker.decreasedMoreThanThreshold(asset3, 30., 300.);
        boolean decreasedMoreThanThreshold4 = strategy1OpportunityChecker.decreasedMoreThanThreshold(asset4, 400., 400.);

        assertFalse(decreasedMoreThanThreshold1);
        assertTrue(decreasedMoreThanThreshold2);
        assertTrue(decreasedMoreThanThreshold3);
        assertFalse(decreasedMoreThanThreshold4);

        verify(strategy1OpportunityChecker, times(1)).logAssetThresholdState(eq(asset1), anyDouble(), eq(100.));
        verify(strategy1OpportunityChecker, times(1)).logAssetThresholdState(eq(asset2), anyDouble(), eq(200.));
        verify(strategy1OpportunityChecker, times(1)).logAssetThresholdState(eq(asset3), anyDouble(), eq(300.));
        verify(strategy1OpportunityChecker, times(1)).logAssetThresholdState(eq(asset4), anyDouble(), eq(400.));
    }

    @Test
    @DisplayName("decreasedMoreThanThreshold - error: latest value higher than max high")
    void decreasedMoreThanThresholdErrorLatestValueHigherThanMaxHigh() {
        boolean decreasedMoreThanThreshold = strategy1OpportunityChecker.decreasedMoreThanThreshold(asset1, 100., 99.);

        assertFalse(decreasedMoreThanThreshold);
        verify(logger, times(1)).error("Latest value of {} is higher than its max high, which should not be possible", asset1.getSymbol());
    }

    @Test
    @DisplayName("decreasedMoreThanThreshold - error: max high is 0")
    void decreasedMoreThanThresholdErrorMaxHighIs0() {
        boolean decreasedMoreThanThreshold = strategy1OpportunityChecker.decreasedMoreThanThreshold(asset1, 99., 0.);

        assertFalse(decreasedMoreThanThreshold);
        verify(logger, times(1)).error("Max high of {} is 0, which should not be possible", asset1.getSymbol());
    }

    @Test
    @DisplayName("logAssetThresholdState - should buy")
    void logAssetThresholdStatePositive() {
        strategy1OpportunityChecker.logAssetThresholdState(asset1, 2.5, 100.);

        verify(logger, times(1)).info(
                "[{}📉 {}%] {}: [Highest: ${}] [Latest: {}$]",
                "🟢",
                "2.50",
                "ASSET1/TEST",
                100.0d,
                0.0d
        );
    }

    @Test
    @DisplayName("logAssetThresholdState - should not buy")
    void logAssetThresholdStateNegative() {
        strategy1OpportunityChecker.logAssetThresholdState(asset1, 1.5, 100.);

        verify(logger, times(1)).info(
                "[{}📉 {}%] {}: [Highest: ${}] [Latest: {}$]",
                "🔴",
                "1.50",
                "ASSET1/TEST",
                100.0d,
                0.0d
        );
    }

    @Test
    @DisplayName("removeAssetsAlreadyBought")
    void removeAssetsAlreadyBought() {
        doReturn(true).when(strategy1OpportunityChecker).checkAssetUncompletedTickets(asset1);
        doReturn(true).when(strategy1OpportunityChecker).checkAssetUncompletedTickets(asset2);
        doReturn(false).when(strategy1OpportunityChecker).checkAssetUncompletedTickets(asset3);
        doReturn(false).when(strategy1OpportunityChecker).checkAssetUncompletedTickets(asset4);

        List<AssetModel> assets = strategy1OpportunityChecker.removeAssetsAlreadyBought(fourAssets);

        assertEquals(twoAssets, assets);
        verify(logger, times(1)).info("Number of opportunities: {}/{}", 2, 4);
    }

    @Test
    @DisplayName("checkAssetUncompletedTickets - no ticket in DB")
    void checkAssetUncompletedTicketsNoTicketInDB() {
        List<Strategy1TicketModel> tickets = new ArrayList<>();
        when(strategy1TicketRepository.findUncompletedTickets(asset1.getSymbol())).thenReturn(tickets);

        boolean checkAssetUncompletedTickets = strategy1OpportunityChecker.checkAssetUncompletedTickets(asset1);

        assertTrue(checkAssetUncompletedTickets);
        verify(logger, times(1)).info("🟢 No uncompleted {} tickets in database", asset1.getSymbol());
    }

    @Test
    @DisplayName("checkAssetUncompletedTickets - tickets in DB but no similar buying price")
    void checkAssetUncompletedTicketsTicketsInDBNoSimilarBuyingPrice() {
        List<Strategy1TicketModel> tickets = new ArrayList<>();
        tickets.add(new Strategy1TicketModel()); // Not empty database

        when(strategy1TicketRepository.findUncompletedTickets(asset1.getSymbol())).thenReturn(tickets);
        doReturn(100.).when(strategy1OpportunityChecker).minBuyPriceFromTicketList(tickets);

        asset1.setLatestValue(90.);
        boolean checkAssetUncompletedTickets = strategy1OpportunityChecker.checkAssetUncompletedTickets(asset1);

        assertTrue(checkAssetUncompletedTickets);
        verify(logger, times(1)).info(
                "🟢 Min value of {} tickets in DB is high enough ({}) compared to current value ({})",
                "ASSET1/TEST",
                100.0d,
                90.0d
        );
    }

    @Test
    @DisplayName("checkAssetUncompletedTickets - tickets in DB and similar buying price")
    void checkAssetUncompletedTicketsTicketsInDBSimilarBuyingPrice() {
        List<Strategy1TicketModel> tickets = new ArrayList<>();
        tickets.add(new Strategy1TicketModel()); // Not empty database

        when(strategy1TicketRepository.findUncompletedTickets(asset1.getSymbol())).thenReturn(tickets);
        doReturn(100.).when(strategy1OpportunityChecker).minBuyPriceFromTicketList(tickets);

        asset1.setLatestValue(91.);
        boolean checkAssetUncompletedTickets = strategy1OpportunityChecker.checkAssetUncompletedTickets(asset1);

        assertFalse(checkAssetUncompletedTickets);
        verify(logger, times(1)).info(
                "🔴 Min value of {} tickets in DB is NOT high enough ({}) compared to current value ({})",
                "ASSET1/TEST",
                100.0d,
                91.0d
        );
    }

    @Test
    @DisplayName("Minimum buyPrice value of list of tickets is returned")
    void minBuyPriceFromTicketList() {
        Strategy1TicketModel ticket1 = new Strategy1TicketModel();
        ticket1.setAverageFilledBuyPrice(100);
        Strategy1TicketModel ticket2 = new Strategy1TicketModel();
        ticket2.setAverageFilledBuyPrice(200);

        List<Strategy1TicketModel> tickets = new ArrayList<>();
        assertEquals(-1, strategy1OpportunityChecker.minBuyPriceFromTicketList(tickets));

        tickets.add(ticket2);
        assertEquals(200, strategy1OpportunityChecker.minBuyPriceFromTicketList(tickets));

        tickets.add(ticket1);
        assertEquals(100, strategy1OpportunityChecker.minBuyPriceFromTicketList(tickets));
    }

}