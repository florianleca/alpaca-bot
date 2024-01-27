package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Date;

@Getter
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

    @JsonProperty("t")
    private Date date;

}
