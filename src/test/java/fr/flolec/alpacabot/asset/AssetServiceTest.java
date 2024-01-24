package fr.flolec.alpacabot.asset;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AssetServiceTest {

    @Autowired
    private AssetService assetService;

    private final Logger logger = LoggerFactory.getLogger(AssetServiceTest.class);

    @DisplayName("Each retrieved asset is active")
    @Test
    void getAssetsList() throws IOException {
        List<AssetModel> assetsList = assetService.getAssetsList();
        logger.info("List of retrieved crypto assets in USD:");
        assetsList.forEach(asset -> {
            logger.info("** " + asset);
            assertEquals("active", asset.getStatus());
        });
    }
}