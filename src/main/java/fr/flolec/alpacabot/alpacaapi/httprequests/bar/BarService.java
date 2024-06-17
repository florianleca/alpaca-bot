package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.HttpRequestService;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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
     * @param asset            The asset of which we want to retrieve the bars
     * @param barTimeFrame     The time duration of a single bar (candle)
     * @param periodLength     How long should be the history period
     * @param periodLengthUnit Unit of the history period
     * @return A list of bars of the given asset for the given history period
     * @throws IOException If an I/O error occurs while fetching or processing the data
     */
    public List<BarModel> getHistoricalBars(AssetModel asset, BarTimeFrame barTimeFrame, long periodLength, PeriodLengthUnit periodLengthUnit) throws IOException {
        String url = Objects.requireNonNull(HttpUrl.parse(endpoint)).newBuilder()
                .addQueryParameter("symbols", asset.getSymbol())
                .addQueryParameter("timeframe", barTimeFrame.getLabel())
                .addQueryParameter("start", periodLengthUnit.goBackInTime(OffsetDateTime.now(), periodLength))
                .addQueryParameter("end", PeriodLengthUnit.now())
                .toString();
        Response response = httpRequestService.get(url);
        assert response.body() != null;
        JsonNode jsonNode = objectMapper.readTree(response.body().string()).path("bars").path(asset.getSymbol());
        return objectMapper.treeToValue(jsonNode, new TypeReference<ArrayList<BarModel>>() {
        });
    }


    public double getMaxHighOnPeriod(AssetModel asset, BarTimeFrame barTimeFrame, long periodLength, PeriodLengthUnit periodLengthUnit) throws IOException {
        List<BarModel> bars = getHistoricalBars(asset, barTimeFrame, periodLength, periodLengthUnit);
        return bars.stream().mapToDouble(BarModel::getHigh).max().orElse(-1);
    }

}
