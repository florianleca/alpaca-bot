package fr.flolec.alpacabot.alpacaapi.httprequests.order;

public enum OrderFieldsNames {
    SYMBOL("symbol"),
    SIDE("side"),
    TYPE("type"),
    TIME_IN_FORCE("time_in_force"),
    LIMIT_PRICE("limit_price"),
    NOTIONAL("notional"),
    QUANTITY("qty");

    public final String label;

    OrderFieldsNames(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
