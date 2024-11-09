package fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Component
public class AccountDetailsService {

    private final RestClient restClient;
    private final Logger logger = LoggerFactory.getLogger(AccountDetailsService.class);
    @Value("${ALPACA_API_ACCOUNT_URI}")
    private String uri;

    public AccountDetailsService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @return Details of account linked to given keys
     */
    public AccountDetailsModel getAccountDetails() {
        try {
            ResponseEntity<AccountDetailsModel> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(AccountDetailsModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.warn("Account details could not be retrieved: {}", e.getMessage());
            return null;
        }
    }

}
