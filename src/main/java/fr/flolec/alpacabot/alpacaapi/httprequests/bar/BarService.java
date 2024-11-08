package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class BarService {

    private final RestClient restClient;
    private final Logger logger = LoggerFactory.getLogger(BarService.class);
    @Value("${ALPACA_DATA_BARS_URI}")
    private String uri;

    public BarService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @param assetSymbol      The asset of which we want to retrieve the bars
     * @param barTimeFrame     The time duration of a single bar (candle)
     * @param periodLength     How long should be the history period
     * @param periodLengthUnit Unit of the history period
     * @return A list of bars of the given asset for the given history period
     */
    public List<BarModel> getHistoricalBars(String assetSymbol, BarTimeFrame barTimeFrame, long periodLength, PeriodLengthUnit periodLengthUnit) {
        List<BarModel> bars = new ArrayList<>();
        String nextPageToken = "";

        while (nextPageToken != null) {
            nextPageToken = getHistoricalBarsPage(assetSymbol, barTimeFrame, periodLength, periodLengthUnit, nextPageToken, bars);
        }

        return bars;
    }

    private String getHistoricalBarsPage(String assetSymbol, BarTimeFrame barTimeFrame, long periodLength, PeriodLengthUnit periodLengthUnit, String nextPageToken, List<BarModel> bars) {
        try {
            ResponseEntity<HistoricalBarsResponse> response = restClient.get()
                    .uri(UriComponentsBuilder
                            .fromUriString(uri)
                            .queryParam("symbols", assetSymbol)
                            .queryParam("timeframe", barTimeFrame.getLabel())
                            .queryParam("start", periodLengthUnit.goBackInTime(OffsetDateTime.now(), periodLength))
                            .queryParam("page_token", nextPageToken)
                            .toUriString())
                    .retrieve()
                    .toEntity(HistoricalBarsResponse.class);

            HistoricalBarsResponse historicalBarsResponse = response.getBody();
            if (historicalBarsResponse != null) {
                bars.addAll(historicalBarsResponse.getBars().get(assetSymbol));
                return historicalBarsResponse.getNextPageToken();
            }
        } catch (HttpStatusCodeException e) {
            logger.warn("Historical bars for {} could not be retrieved: {}", assetSymbol, e.getMessage());
        }
        bars.clear();
        return null;
    }

    @Getter
    @Setter
    public static class HistoricalBarsResponse {

        @JsonProperty("bars")
        private Map<String, List<BarModel>> bars;

        @JsonProperty("next_page_token")
        private String nextPageToken;

    }

}
