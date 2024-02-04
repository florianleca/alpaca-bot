package fr.flolec.alpacabot.strategies.strategy1;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "tickets-strategy-1")
public class Strategy1Ticket {

    @Field("symbol")
    private String symbol;

    @Id
    @Field("buy_order_id")
    private String buyOrderId;

    @Field("sell_order_id")
    private String sellOrderId;

    @Field("status")
    private Strategy1TicketStatus status;

    @Field("position_qty_before_buying")
    private double positionQtyBeforeBuyOrder;

    @Field("position_qty_after_buying")
    private double positionQtyAfterBuyOrder;

    @Field("quantity_to_sell")
    private double quantityToSell;

}
