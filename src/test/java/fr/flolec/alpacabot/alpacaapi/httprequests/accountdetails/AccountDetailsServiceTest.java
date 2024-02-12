package fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails;

import fr.flolec.alpacabot.AlpacaBotApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AlpacaBotApplication.class)
class AccountDetailsServiceTest {

    @Autowired
    private AccountDetailsService accountDetailsService;

    @DisplayName("Account details are retrieved ; its status is active")
    @Test
    void getAccountDetails() throws IOException {
        AccountDetailsModel accountInformations = accountDetailsService.getAccountDetails();
        assertNotNull(accountInformations.getId());
        assertNotNull(accountInformations.getAccountNumber());
        assertNotNull(accountInformations.getCreatedAt());
        assertNotNull(accountInformations.getPendingTransferIn());
        assertEquals("ACTIVE", accountInformations.getStatus());
        assertEquals("ACTIVE", accountInformations.getCryptoStatus());
        assertEquals("USD", accountInformations.getCurrency());
        assertFalse(accountInformations.getAccountBlocked());
        assertFalse(accountInformations.getTradingBlocked());
        assertFalse(accountInformations.getTransfersBlocked());
        double cash = Double.parseDouble(accountInformations.getCash());
        double equity = Double.parseDouble(accountInformations.getEquity());
        assertTrue(equity >= cash);
    }
}