package fr.flolec.alpacabot.alpacaapi.accountdetails;

import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Component
public class AccountDetailsService {
    private final RestClient restClient;
    @Value("${ALPACA_API_ACCOUNT_URI}")
    private String uri;

    public AccountDetailsService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @return Details of account linked to given keys
     */
    public AccountDetailsModel getAccountDetails() throws AlpacaApiException {
        try {
            ResponseEntity<AccountDetailsModel> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(AccountDetailsModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new AlpacaApiException(e, "Account details could not be retrieved");
        }
    }

}
