package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarService;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Strategy1OpportunityCheckerTest {

    private final double threshold = 1;
    @Value("${TIMEFRAME}")
    private String barTimeFrameLabel;
    @Value("${PERIOD_LENGTH}")
    private int periodLength;
    @Value("${PERIOD_LENGTH_UNIT}")
    private String periodLengthUnitLabel;

    @Mock
    private AssetService assetService;

    @Mock
    private Strategy1Service strategy1Service;

    @Mock
    private LatestQuoteService latestQuoteService;

    @Mock
    private BarService barService;

    @Spy
    @InjectMocks
    private Strategy1OpportunityChecker strategy1OpportunityChecker;

    private AssetModel asset1;
    private AssetModel asset2;
    private AssetModel asset3;
    private AssetModel asset4;

    private List<AssetModel> fourAssets;
    private List<AssetModel> threeAssets;
    private List<AssetModel> twoAssets;

    @BeforeEach
    void setUp() {
        asset1 = new AssetModel("asset1");
        asset2 = new AssetModel("asset2");
        asset3 = new AssetModel("asset3");
        asset4 = new AssetModel("asset4");

        fourAssets = Arrays.asList(asset1, asset2, asset3, asset4);
        threeAssets = Arrays.asList(asset1, asset2, asset3);
        twoAssets = Arrays.asList(asset1, asset2);
    }


    @Test
    @DisplayName("Launching Strategy 1 opportunity checker")
    void checkBuyOpportunities() throws IOException {
        when(assetService.getAssetsList()).thenReturn(fourAssets);
        doReturn(threeAssets).when(strategy1OpportunityChecker).removeAssetsUnderThreshold(fourAssets);
        doReturn(twoAssets).when(strategy1OpportunityChecker).removeAssetsAlreadyBought(threeAssets);

        strategy1OpportunityChecker.checkBuyOpportunities();

        verify(assetService, times(1)).getAssetsList();
        verify(strategy1Service, times(2)).createBuyOrder(any());
    }

    @Test
    @DisplayName("removeAssetsUnderThreshold - nominal")
    void removeAssetsUnderThresholdNominal() throws IOException {

        when(latestQuoteService.getLatestQuote(asset1)).thenReturn(100.);
        when(latestQuoteService.getLatestQuote(asset2)).thenReturn(200.);
        when(latestQuoteService.getLatestQuote(asset3)).thenReturn(300.);

        when(barService.getMaxHighOnPeriod(asset1, barTimeFrameLabel, periodLength, periodLengthUnitLabel)).thenReturn(100.);
        when(barService.getMaxHighOnPeriod(asset2, barTimeFrameLabel, periodLength, periodLengthUnitLabel)).thenReturn(200. / (1 - (threshold / 100.)));
        when(barService.getMaxHighOnPeriod(asset3, barTimeFrameLabel, periodLength, periodLengthUnitLabel)).thenReturn(3000.);

        strategy1OpportunityChecker.setThreshold(1);
        List<AssetModel> returnedAssets = strategy1OpportunityChecker.removeAssetsUnderThreshold(threeAssets);

        assertFalse(returnedAssets.contains(asset1));
        assertTrue(returnedAssets.contains(asset2));
        assertTrue(returnedAssets.contains(asset3));

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