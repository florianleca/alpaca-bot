package fr.flolec.alpacabot.strategies.strategy1;

public enum Strategy1TicketStatus {
    BUY_FILLED("Buy Order Filled, Sell Order not created yet"),
    SELL_UNFILLED("Buy Order Filled, Sell Order Unfilled"),
    COMPLETE("Complete: both orders filled");

    private final String label;

    Strategy1TicketStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
