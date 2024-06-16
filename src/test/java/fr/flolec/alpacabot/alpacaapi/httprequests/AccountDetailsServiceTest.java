package fr.flolec.alpacabot.alpacaapi.httprequests;

import fr.flolec.alpacabot.WireMockedTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails.AccountDetailsModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails.AccountDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountDetailsServiceTest extends WireMockedTest {

    public static final String ACCOUNT_RESPONSE_BODY_EXAMPLE = "{\"id\":\"1a1acdf8-1b68-4aa1-907a-96398f11cf7b\",\"admin_configurations\":{},\"user_configurations\":null,\"account_number\":\"PA38SUVRBQQ2\",\"status\":\"ACTIVE\",\"crypto_status\":\"ACTIVE\",\"options_approved_level\":2,\"options_trading_level\":2,\"currency\":\"USD\",\"buying_power\":\"98.5\",\"regt_buying_power\":\"98.5\",\"daytrading_buying_power\":\"0\",\"effective_buying_power\":\"98.5\",\"non_marginable_buying_power\":\"98.5\",\"options_buying_power\":\"98.5\",\"bod_dtbp\":\"0\",\"cash\":\"98.5\",\"accrued_fees\":\"0\",\"pending_transfer_in\":\"0\",\"portfolio_value\":\"98.5\",\"pattern_day_trader\":false,\"trading_blocked\":false,\"transfers_blocked\":false,\"account_blocked\":false,\"created_at\":\"2024-03-26T12:42:33.070311Z\",\"trade_suspended_by_user\":false,\"multiplier\":\"1\",\"shorting_enabled\":false,\"equity\":\"98.5\",\"last_equity\":\"98.72\",\"long_market_value\":\"0\",\"short_market_value\":\"0\",\"position_market_value\":\"0\",\"initial_margin\":\"0\",\"maintenance_margin\":\"0\",\"last_maintenance_margin\":\"0\",\"sma\":\"98.78\",\"daytrade_count\":0,\"balance_asof\":\"2024-05-22\",\"crypto_tier\":1,\"intraday_adjustments\":\"0\",\"pending_reg_taf_fees\":\"0\"}\n";
    private AccountDetailsService accountDetailsService;

    @BeforeEach
    public void setUp() {
        accountDetailsService = new AccountDetailsService("http://localhost:8080/account", objectMapper, httpRequestService);
    }

    @Test
    @DisplayName("Account details are retrieved and serialized correctly")
    public void testGetAccountDetails() throws IOException {
        // Configurer la réponse simulée
        stubFor(get(urlEqualTo("/account"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ACCOUNT_RESPONSE_BODY_EXAMPLE)));

        AccountDetailsModel actualAccountDetails = accountDetailsService.getAccountDetails();

        assertNotNull(actualAccountDetails);
        assertEquals("1a1acdf8-1b68-4aa1-907a-96398f11cf7b", actualAccountDetails.getId());
        assertEquals("PA38SUVRBQQ2", actualAccountDetails.getAccountNumber());
        assertEquals("ACTIVE", actualAccountDetails.getStatus());
        assertEquals("ACTIVE", actualAccountDetails.getCryptoStatus());
        assertEquals("USD", actualAccountDetails.getCurrency());
        assertEquals("98.5", actualAccountDetails.getCash());
        assertEquals("0", actualAccountDetails.getPendingTransferIn());
        assertEquals(false, actualAccountDetails.getTradingBlocked());
        assertEquals(false, actualAccountDetails.getTransfersBlocked());
        assertEquals(false, actualAccountDetails.getAccountBlocked());
        assertEquals(Date.from(Instant.parse("2024-03-26T12:42:33.070311Z")), actualAccountDetails.getCreatedAt());
        assertEquals("98.5", actualAccountDetails.getEquity());
    }

}