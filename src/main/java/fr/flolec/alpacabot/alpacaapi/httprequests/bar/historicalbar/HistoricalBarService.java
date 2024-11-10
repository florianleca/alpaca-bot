package fr.flolec.alpacabot.alpacaapi.httprequests.bar.historicalbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.PeriodLengthUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class HistoricalBarService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(HistoricalBarService.class);
    @Value("${ALPACA_DATA_HISTORICAL_BARS_URI_CRYPTO}")
    private String uriCrypto;
    @Value("${ALPACA_DATA_HISTORICAL_BARS_URI_STOCKS}")
    private String uriStocks;

    public HistoricalBarService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    /**
     * @param assetSymbol      The asset of which we want to retrieve the bars
     * @param barTimeFrame     The time duration of a single bar (candle)
     * @param periodLength     How long should be the history period
     * @param periodLengthUnit Unit of the history period
     * @return A list of bars of the given asset for the given history period
     */
    public List<BarModel> getHistoricalBars(String assetSymbol,
                                            BarTimeFrame barTimeFrame,
                                            long periodLength,
                                            PeriodLengthUnit periodLengthUnit,
                                            boolean isCrypto) {
        List<BarModel> bars = new ArrayList<>();
        String nextPageToken = "";

        while (nextPageToken != null) {
            String baseUri = isCrypto ? uriCrypto : uriStocks;
            String start = periodLengthUnit.goBackInTime(OffsetDateTime.now(), periodLength);

            URI uri = UriComponentsBuilder
                    .fromUriString(baseUri)
                    .queryParam("symbols", "{symbols}")
                    .queryParam("timeframe", "{timeframe}")
                    .queryParam("start", "{start}")
                    .queryParam("page_token", "{page_token}")
                    .build(assetSymbol, barTimeFrame.getLabel(), start, nextPageToken);

            nextPageToken = getHistoricalBarsPage(assetSymbol, uri, bars);
        }

        return bars;
    }

    private String getHistoricalBarsPage(String assetSymbol, URI uri, List<BarModel> bars) {
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class);
            JsonNode barsNode = objectMapper.readTree(response.getBody()).path("bars").path(assetSymbol);
            List<BarModel> newBars = objectMapper.treeToValue(barsNode, new TypeReference<>() {
            });
            bars.addAll(newBars);
            String newToken = objectMapper.readTree(response.getBody()).path("next_page_token").asText();
            return "null".equals(newToken) ? null : newToken;
        } catch (HttpStatusCodeException | JsonProcessingException e) {
            logger.warn("Historical bars for {} could not be retrieved: {}", assetSymbol, e.getMessage());
        }
        bars.clear();
        return null;
    }

}
