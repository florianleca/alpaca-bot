package fr.flolec.alpacabot.latestquote;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LatestQuoteServiceTest {

    private final Logger logger = LoggerFactory.getLogger(LatestQuoteServiceTest.class);

    @Autowired
    private LatestQuoteService latestQuoteService;

    @Autowired
    private AssetService assetService;

    @Test
    @DisplayName("The latest quote of an asset is a positive number")
    void getLatestQuote() throws IOException {
        AssetModel unAsset = assetService.getAssetsList().get(0);
        double latestQuote = latestQuoteService.getLatestQuote(unAsset);
        logger.info("Latest quote of asset " + unAsset.getSymbol() + ": $" + latestQuote);
        assertTrue(latestQuote > 0);
    }
}