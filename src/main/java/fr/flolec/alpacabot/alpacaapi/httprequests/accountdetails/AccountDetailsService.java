package fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountDetailsService {

    @Value("${ALPACA_API_ACCOUNT_URI}")
    private String uri;
    private final RestClient restClient;

    public AccountDetailsService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @return Details of account linked to given keys
     */
    public AccountDetailsModel getAccountDetails() {
        ResponseEntity<AccountDetailsModel> response = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(AccountDetailsModel.class);
        return response.getBody();
    }

}
