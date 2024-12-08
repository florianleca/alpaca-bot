package fr.flolec.alpacabot.alpacaapi.bar;

import fr.flolec.alpacabot.alpacaapi.bar.historicalbar.HistoricalBarController;
import fr.flolec.alpacabot.alpacaapi.bar.historicalbar.HistoricalBarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoricalBarController.class)
class HistoricalBarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoricalBarService historicalBarService;

    @Test
    void loadHistoricalBars() throws Exception {
        this.mockMvc.perform(get("/historical-bars/load-db?assetSymbol=AAPL&numberOfBars=10"))
                .andExpect(status().isOk());

        verify(historicalBarService).loadHistoricalBars("AAPL", 10);
    }

    @Test
    void emptyHistoricalBars() throws Exception {
        this.mockMvc.perform(delete("/historical-bars/empty-db"))
                .andExpect(status().isOk());

        verify(historicalBarService).deleteAll();
    }

}