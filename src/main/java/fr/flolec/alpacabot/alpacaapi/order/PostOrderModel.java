package fr.flolec.alpacabot.alpacaapi.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostOrderModel {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("qty")
    private String quantity;

    @JsonProperty("notional")
    private String notional;

    @JsonProperty("side")
    private String side;

    @JsonProperty("type")
    private String type;

    @JsonProperty("time_in_force")
    private String timeInForce;

    @JsonProperty("limit_price")
    private String limitPrice;

}
