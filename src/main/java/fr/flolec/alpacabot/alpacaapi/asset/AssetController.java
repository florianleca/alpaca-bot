package fr.flolec.alpacabot.alpacaapi.asset;

import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assets")
@Slf4j
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/active-tradable")
    public List<AssetModel> activeTradableAssets(@RequestParam("assetClass") AssetClass assetClass) throws AlpacaApiException {
        List<AssetModel> assets = assetService.getAssetsList(assetClass);
        log.info("Retrieved {} active tradable {} assets", assets.size(), assetClass.getLabel());
        return assets;
    }

}
