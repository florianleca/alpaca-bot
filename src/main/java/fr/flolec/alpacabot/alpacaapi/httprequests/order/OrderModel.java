package fr.flolec.alpacabot.alpacaapi.httprequests.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Getter
@Setter
@Document(collection = "orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderModel {

    @Id
    @JsonProperty("id")
    private String id;

    @Field("created_at")
    @JsonProperty("created_at")
    private Date createdAt;

    @Field("filled_at")
    @JsonProperty("filled_at")
    private Date filledAt;

    @Field("canceled_at")
    @JsonProperty("canceled_at")
    private Date canceledAt;

    @Field("symbol")
    @JsonProperty("symbol")
    private String symbol;

    @Field("notional")
    @JsonProperty("notional")
    private String notional;

    @Field("quantity")
    @JsonProperty("qty")
    private String quantity;

    @Field("filled_quantity")
    @JsonProperty("filled_qty")
    private double filledQuantity;

    @Field("filled_avg_price")
    @JsonProperty("filled_avg_price")
    private double filledAvgPrice;

    @Field("order_type")
    @JsonProperty("order_type")
    private String orderType;

    @Field("side")
    @JsonProperty("side")
    private String side;

    @Field("time_in_force")
    @JsonProperty("time_in_force")
    private String timeInForce;

    @Field("limit_price")
    @JsonProperty("limit_price")
    private double limitPrice;

    @Field("status")
    @JsonProperty("status")
    private String status;

    @Field("dual_order_id")
    private String dualOrderId;

    /*
     Unused properties:
          client_order_id
          updated_at
          submitted_at
          expired_at
          failed_at
          replaced_at
          replaced_by
          replaces
          asset_class
          order_class
          stop_price
          extended_hours
          legs
          trail_percent
          trail_price
          hwm
          subtag
          source

     */

}
