package fr.flolec.alpacabot.alpacaapi.bar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "hourly-bars")
@CompoundIndex(name = "unique_bar_index", def = "{'begin_time': 1, 'symbol': 1}", unique = true)
public class BarModel {

    @Id
    private String id;

    private String symbol;

    @JsonProperty("c")
    private double close;

    @JsonProperty("h")
    private double high;

    @JsonProperty("l")
    private double low;

    @JsonProperty("o")
    private double open;

    @JsonProperty("v")
    private double volume;

    @JsonProperty("t")
    @Field("begin_time")
    private Instant beginTime;

    public BarModel(String symbol, String beginTime, double close, double high, double low, double open, double volume) {
        this(beginTime, close, high, low, open, volume);
        this.symbol = symbol;
    }

    public BarModel(String beginTime, double close, double high, double low, double open, double volume) {
        this.close = close;
        this.high = high;
        this.low = low;
        this.open = open;
        this.volume = volume;
        this.beginTime = Instant.parse(beginTime);
    }

}
