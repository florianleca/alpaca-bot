package fr.flolec.alpacabot.alpacaapi.httprequests;

import fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails.AccountDetailsModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails.AccountDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(AccountDetailsService.class)
@ContextConfiguration(classes = {
        RestClientConfiguration.class,
        AccountDetailsService.class})
class AccountDetailsServiceTest {

    public static final String ACCOUNT_RESPONSE_BODY_EXAMPLE = "{\"id\":\"1a1acdf8-1b68-4aa1-907a-96398f11cf7b\",\"admin_configurations\":{},\"user_configurations\":null,\"account_number\":\"PA38SUVRBQQ2\",\"status\":\"ACTIVE\",\"crypto_status\":\"ACTIVE\",\"options_approved_level\":2,\"options_trading_level\":2,\"currency\":\"USD\",\"buying_power\":\"98.5\",\"regt_buying_power\":\"98.5\",\"daytrading_buying_power\":\"0\",\"effective_buying_power\":\"98.5\",\"non_marginable_buying_power\":\"98.5\",\"options_buying_power\":\"98.5\",\"bod_dtbp\":\"0\",\"cash\":\"98.5\",\"accrued_fees\":\"0\",\"pending_transfer_in\":\"0\",\"portfolio_value\":\"98.5\",\"pattern_day_trader\":false,\"trading_blocked\":false,\"transfers_blocked\":false,\"account_blocked\":false,\"created_at\":\"2024-03-26T12:42:33.070311Z\",\"trade_suspended_by_user\":false,\"multiplier\":\"1\",\"shorting_enabled\":false,\"equity\":\"98.5\",\"last_equity\":\"98.72\",\"long_market_value\":\"0\",\"short_market_value\":\"0\",\"position_market_value\":\"0\",\"initial_margin\":\"0\",\"maintenance_margin\":\"0\",\"last_maintenance_margin\":\"0\",\"sma\":\"98.78\",\"daytrade_count\":0,\"balance_asof\":\"2024-05-22\",\"crypto_tier\":1,\"intraday_adjustments\":\"0\",\"pending_reg_taf_fees\":\"0\"}\n";

    @Value("${ALPACA_API_ACCOUNT_URI}")
    private String uri;

    @Autowired
    private AccountDetailsService accountDetailsService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(accountDetailsService, "logger", logger);
    }

    @Test
    @DisplayName("getAccountDetails: nominal -> account details retrieved")
    void getAccountDetails_nominal_accountDetailsRetrieved() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andRespond(withSuccess(ACCOUNT_RESPONSE_BODY_EXAMPLE, MediaType.APPLICATION_JSON));

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

    @Test
    @DisplayName("getAccountDetails: error -> nothing retrieved & logged error")
    void getAccountDetails_error_nothingRetrievedAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Forbidden\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        AccountDetailsModel actualAccountDetails = accountDetailsService.getAccountDetails();

        assertNull(actualAccountDetails);
        verify(logger).warn("Account details could not be retrieved: {}", "403 Forbidden: \"{\"message\":\"Forbidden\"}\"");
    }

}