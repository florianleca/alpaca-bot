package fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AccountDetailsService {


    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;

    @Autowired
    public AccountDetailsService(@Value("${PAPER_ACCOUNT_ENDPOINT}") String endpoint,
                                 ObjectMapper objectMapper,
                                 HttpRequestService httpRequestService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
    }

    /**
     * @return Details of account linked to given keys
     * @throws IOException If an I/O error occurs while fetching or processing the data
     */
    public AccountDetailsModel getAccountDetails() throws IOException {
        Response response = httpRequestService.get(endpoint);
        JsonNode jsonNode = objectMapper.readTree(response.body().string());
        return objectMapper.treeToValue(jsonNode, AccountDetailsModel.class);
    }

}
