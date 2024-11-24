package fr.flolec.alpacabot.alpacaapi.asset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AssetService assetService;

    @Test
    void subscribeToCryptoAssets() throws Exception {
        when(assetService.subscribeToCryptoAssets()).thenReturn(List.of());

        this.mockMvc.perform(post("/assets/subscribe-to-crypto"))
                .andExpect(status().isOk());

        verify(assetService).subscribeToCryptoAssets();
    }

}