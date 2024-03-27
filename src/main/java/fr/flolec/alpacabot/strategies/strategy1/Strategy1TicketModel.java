package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "tickets-strategy-1")
public class Strategy1TicketModel {

    @Id
    @Field("id")
    private ObjectId id;

    @Field("symbol")
    private String symbol;

    @Field("buy_order_id")
    private String buyOrderId;

    @Field("sell_order_id")
    private String sellOrderId;

    @Field("status")
    private Strategy1TicketStatus status;

    @Field("bought_quantity")
    private double boughtQuantity;

    @Field("average_filled_buy_price")
    private double averageFilledBuyPrice;

    @Field("sold_quantity")
    private double soldQuantity;

    @Field("average_filled_sell_price")
    private double averageFilledSellPrice;

    public Strategy1TicketModel() {
    }


    public Strategy1TicketModel(OrderModel buyOrder) throws IllegalArgumentException {
        if (!buyOrder.getSide().equals("buy")) {
            throw new IllegalArgumentException("Trying to create a ticket without a buy order: " + buyOrder.getSide());
        }
        if (!buyOrder.getStatus().equals("filled")) {
            throw new IllegalArgumentException("Trying to create a ticket with an unfilled buy order: " + buyOrder.getStatus());
        }
        this.symbol = buyOrder.getSymbol();
        this.buyOrderId = buyOrder.getId();
        this.boughtQuantity = buyOrder.getFilledQuantity();
        this.averageFilledBuyPrice = buyOrder.getFilledAvgPrice();
    }

    @Override
    public String toString() {
        return "Strategy1TicketModel{" +
                "symbol='" + symbol + '\'' +
                ", status=" + status +
                '}';
    }
}
