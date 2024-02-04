package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "tickets-strategy-1")
public class Strategy1TicketModel {

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


    public Strategy1TicketModel(OrderModel buyOrder, double positionQtyBeforeBuyOrder) {
        this.symbol = buyOrder.getSymbol();
        this.buyOrderId = buyOrder.getId();
        this.positionQtyBeforeBuyOrder = positionQtyBeforeBuyOrder;
        this.status = Strategy1TicketStatus.BUY_UNFILLED;
    }

    @Override
    public String toString() {
        return "Strategy1TicketModel{" +
                "symbol='" + symbol + '\'' +
                ", status=" + status +
                '}';
    }
}
