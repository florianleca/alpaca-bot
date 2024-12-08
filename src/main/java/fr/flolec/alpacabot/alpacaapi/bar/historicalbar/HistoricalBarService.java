package fr.flolec.alpacabot.alpacaapi.bar.historicalbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import fr.flolec.alpacabot.alpacaapi.bar.BarModel;
import fr.flolec.alpacabot.alpacaapi.bar.BarModelRepository;
import fr.flolec.alpacabot.alpacaapi.bar.BarTimeFrame;
import fr.flolec.alpacabot.alpacaapi.bar.PeriodLengthUnit;
import lombok.extern.slf4j.Slf4j;
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


@Slf4j
@Component
public class HistoricalBarService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final BarModelRepository barModelRepository;
    @Value("${ALPACA_DATA_HISTORICAL_BARS_URI_CRYPTO}")
    private String uriCrypto;
    @Value("${ALPACA_DATA_HISTORICAL_BARS_URI_STOCKS}")
    private String uriStocks;
    @Value("${MAX_BARS_PER_SYMBOL}")
    private int maxBarsPerSymbol = 10;

    public HistoricalBarService(RestClient restClient, ObjectMapper objectMapper, BarModelRepository barModelRepository) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.barModelRepository = barModelRepository;
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
                                            boolean isCrypto) throws AlpacaApiException, JsonProcessingException {
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
        bars.forEach(bar -> bar.setSymbol(assetSymbol));
        log.info("Retrieved {} bars for '{}'", bars.size(), assetSymbol);
        return bars;
    }

    private String getHistoricalBarsPage(String assetSymbol, URI uri, List<BarModel> bars) throws AlpacaApiException, JsonProcessingException {
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
        } catch (HttpStatusCodeException e) {
            String message = String.format("Historical bars for '%s' could not be retrieved", assetSymbol);
            throw new AlpacaApiException(e, message);
        }
    }

    public List<BarModel> getNumberOfHourlyBars(String assetSymbol, int numberOfBars) throws AlpacaApiException, JsonProcessingException {
        String start = PeriodLengthUnit.HOUR.goBackInTime(OffsetDateTime.now(), numberOfBars + 100L);
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(UriComponentsBuilder
                            .fromUriString(uriStocks)
                            .queryParam("symbols", "{symbols}")
                            .queryParam("timeframe", "{timeframe}")
                            .queryParam("start", "{start}")
                            .queryParam("limit", "{limit}")
                            .queryParam("sort", "{sort}")
                            .build(assetSymbol, BarTimeFrame.HOUR1.getLabel(), start, numberOfBars, "desc"))
                    .retrieve()
                    .toEntity(String.class);
            JsonNode barsNode = objectMapper.readTree(response.getBody()).path("bars").path(assetSymbol);
            List<BarModel> bars = objectMapper.treeToValue(barsNode, new TypeReference<>() {
            });
            bars.forEach(bar -> bar.setSymbol(assetSymbol));
            log.info("Retrieved {} bars for '{}'", bars.size(), assetSymbol);
            return bars;
        } catch (HttpStatusCodeException e) {
            String message = String.format("Historical bars for '%s' could not be retrieved", assetSymbol);
            throw new AlpacaApiException(e, message);
        }
    }

    public List<String> loadHistoricalBars(String assetSymbol, int numberOfBars) throws AlpacaApiException, JsonProcessingException {
        List<BarModel> bars = getNumberOfHourlyBars(assetSymbol, numberOfBars);
        bars.forEach(barModelRepository::insertOrReplace);

        // Clean excess bars
        barModelRepository.cleanExcessBars(assetSymbol, maxBarsPerSymbol);

        return List.of(assetSymbol);
    }

    public void deleteAll() {
        barModelRepository.deleteAll();
    }

}
