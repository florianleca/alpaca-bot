package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;


@Component
public class BarService {

    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpRequestService httpRequestService;

    @Autowired
    public BarService(@Value("${PAPER_BARS_ENDPOINT}") String endpoint,
                      ObjectMapper objectMapper,
                      HttpRequestService httpRequestService) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper;
        this.httpRequestService = httpRequestService;
    }


    /**
     * @param assetSymbol      The asset of which we want to retrieve the bars
     * @param barTimeFrame     The time duration of a single bar (candle)
     * @param periodLength     How long should be the history period
     * @param periodLengthUnit Unit of the history period
     * @return A list of bars of the given asset for the given history period
     * @throws IOException If an I/O error occurs while fetching or processing the data
     */
    public List<BarModel> getHistoricalBars(String assetSymbol, BarTimeFrame barTimeFrame, long periodLength, PeriodLengthUnit periodLengthUnit) throws IOException {
        List<BarModel> bars = new ArrayList<>();
        String nextPageToken = "";

        do {
            String url = requireNonNull(HttpUrl.parse(endpoint)).newBuilder()
                    .addQueryParameter("symbols", assetSymbol)
                    .addQueryParameter("timeframe", barTimeFrame.getLabel())
                    .addQueryParameter("start", periodLengthUnit.goBackInTime(OffsetDateTime.now(), periodLength))
                    .addQueryParameter("page_token", nextPageToken)
                    .toString();

            Response response = httpRequestService.get(url);
            if (response.body() == null) throw new IOException("Response body is null");
            String responseString = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseString).path("bars").path(assetSymbol);
            bars.addAll(objectMapper.treeToValue(jsonNode, new TypeReference<ArrayList<BarModel>>() {
            }));
            nextPageToken = objectMapper.readTree(responseString).path("next_page_token").asText();
        } while (!nextPageToken.equals("null"));

        return bars;
    }

}
