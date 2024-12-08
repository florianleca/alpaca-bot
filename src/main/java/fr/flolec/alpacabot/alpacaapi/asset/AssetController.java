package fr.flolec.alpacabot.alpacaapi.asset;

import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping("/subscribe-to-crypto")
    public List<String> subscribeToCryptoAssets() throws AlpacaApiException, IOException {
        return assetService.subscribeToCryptoAssets();
    }

}
