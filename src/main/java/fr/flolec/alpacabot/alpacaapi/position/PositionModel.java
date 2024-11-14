package fr.flolec.alpacabot.alpacaapi.position;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionModel {

    @JsonProperty("asset_id")
    private String assetId;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("asset_class")
    private String assetClass;

    @JsonProperty("qty")
    private String quantity;

    @JsonProperty("avg_entry_price")
    private String averageEntryPrice;

    @JsonProperty("side")
    private String side;

    @JsonProperty("market_value")
    private String marketValue;

    @JsonProperty("cost_basis")
    private String costBasis;

    @JsonProperty("current_price")
    private String currentPrice;

    @JsonProperty("lastday_price")
    private String lastDayPrice;

    @JsonProperty("change_today")
    private String changeToday;

    @JsonProperty("qty_available")
    private String quantityAvailable;

}
