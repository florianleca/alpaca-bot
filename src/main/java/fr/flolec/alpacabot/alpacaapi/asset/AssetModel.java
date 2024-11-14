package fr.flolec.alpacabot.alpacaapi.asset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetModel {

    @JsonProperty("id")
    private String id;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("tradable")
    private Boolean tradable;

    @JsonProperty("fractionable")
    private Boolean fractionable;

    @JsonProperty("min_order_size")
    private String minOrderSize;

    @JsonProperty("min_trade_increment")
    private String minTradeIncrement;

    @JsonProperty("price_increment")
    private String priceIncrement;

}
