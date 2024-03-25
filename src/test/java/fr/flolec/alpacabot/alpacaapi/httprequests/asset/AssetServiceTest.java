package fr.flolec.alpacabot.alpacaapi.httprequests.asset;

import fr.flolec.alpacabot.AlpacaBotApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AlpacaBotApplication.class)
class AssetServiceTest {

    private final Logger logger = LoggerFactory.getLogger(AssetServiceTest.class);

    @Autowired
    private AssetService assetService;

    @DisplayName("Each retrieved asset is active")
    @Test
    void getAssetsList() throws IOException {
        List<AssetModel> assetsList = assetService.getAssetsList();
        logger.info("List of the {} retrieved crypto assets in USD:", assetsList.size());
        assetsList.forEach(asset -> {
            logger.info("** {} **", asset.getSymbol());
            assertNotNull(asset.getId());
            assertNotNull(asset.getMinOrderSize());
            assertNotNull(asset.getMinTradeIncrement());
            assertEquals("active", asset.getStatus());
            assertEquals("CRYPTO", asset.getExchange());
            assertTrue(asset.getSymbol().contains("/USD"));
            assertTrue(asset.getTradable());
            assertTrue(asset.getFractionable());
            assertEquals(0, asset.getLatestValue());
        });
    }
}