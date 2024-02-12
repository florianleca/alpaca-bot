package fr.flolec.alpacabot.strategies.strategy1;

public enum Strategy1TicketStatus {
    BUY_UNFILLED("(1/3) Buy Order Unfilled"),
    BUY_FILLED_SELL_UNFILLED("(2/3) Buy Order Filled, Sell Order Unfilled"),
    COMPLETE("(3/3) Complete: both orders filled");

    private final String label;

    Strategy1TicketStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
