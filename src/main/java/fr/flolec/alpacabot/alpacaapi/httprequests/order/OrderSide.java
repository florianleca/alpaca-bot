package fr.flolec.alpacabot.alpacaapi.httprequests.order;

public enum OrderSide {

    BUY("buy"),
    SELL("sell");

    public final String side;

    OrderSide(String side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return this.side;
    }
}
