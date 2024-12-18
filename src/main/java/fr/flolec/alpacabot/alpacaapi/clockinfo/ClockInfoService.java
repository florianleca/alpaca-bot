package fr.flolec.alpacabot.alpacaapi.clockinfo;

import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Service
public class ClockInfoService {

    private final RestClient restClient;
    @Value("${ALPACA_API_CLOCK_URI}")
    private String uri;

    public ClockInfoService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ClockInfoModel getClockInfo() throws AlpacaApiException {
        try {
            ResponseEntity<ClockInfoModel> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(ClockInfoModel.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new AlpacaApiException(e, "Clock info could not be retrieved");
        }
    }

}
