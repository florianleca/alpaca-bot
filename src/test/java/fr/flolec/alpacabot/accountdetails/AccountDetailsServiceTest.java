package fr.flolec.alpacabot.accountdetails;

import fr.flolec.alpacabot.AlpacaBotApplication;
import fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails.AccountDetailsModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails.AccountDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = AlpacaBotApplication.class)
public class AccountDetailsServiceTest {

    @Autowired
    private AccountDetailsService accountDetailsService;

    @DisplayName("Account details are retrieved ; its status is active")
    @Test
    void getAccountDetails() throws IOException {
        AccountDetailsModel accountInformations = accountDetailsService.getAccountDetails();
        assertEquals("ACTIVE", accountInformations.getStatus());
        assertEquals("ACTIVE", accountInformations.getCryptoStatus());
        assertEquals("USD", accountInformations.getCurrency());
    }
}