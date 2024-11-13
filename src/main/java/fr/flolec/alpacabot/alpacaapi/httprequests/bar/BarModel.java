package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BarModel {

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
    private String beginTime;

    public BarModel(String beginTime, double close, double high, double low, double open, double volume) {
        this.close = close;
        this.high = high;
        this.low = low;
        this.open = open;
        this.volume = volume;
        this.beginTime = beginTime;
    }

}
