package fr.flolec.alpacabot.alpacaapi.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.AlpacaWebSocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetServiceSpyTest {

    @Spy
    @InjectMocks
    private AssetService assetService;

    @Mock
    private AlpacaWebSocketService alpacaWebSocketService;

    @Mock
    private RestClient restClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void subscribeToCryptoAssets_nominal_rightSubscriptionAndListReturned() throws AlpacaApiException, IOException {
        // Given
        AssetModel assetModel1 = new AssetModel();
        assetModel1.setSymbol("BTC/USD");
        AssetModel assetModel2 = new AssetModel();
        assetModel2.setSymbol("ETH/USD");
        AssetModel assetModel3 = new AssetModel();
        assetModel3.setSymbol("LTC/USD");
        AssetModel assetModel4 = new AssetModel();
        assetModel4.setSymbol("DOGE/USD");
        List<AssetModel> assetModels = new ArrayList<>(Arrays.asList(assetModel1, assetModel2, assetModel3, assetModel4));

        doReturn(assetModels).when(assetService).getAssetsList(AssetClass.CRYPTO);

        // When
        List<String> assetNames = assetService.subscribeToCryptoAssets();

        // Then
        assertEquals("BTC/USD, ETH/USD, LTC/USD, DOGE/USD", String.join(", ", assetNames));
        verify(alpacaWebSocketService).sendMessage("{\"action\":\"subscribe\",\"bars\":[\"BTC/USD\",\"ETH/USD\",\"LTC/USD\",\"DOGE/USD\"]}");
    }

}
