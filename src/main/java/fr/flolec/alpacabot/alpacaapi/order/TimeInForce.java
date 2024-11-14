package fr.flolec.alpacabot.alpacaapi.order;

public enum TimeInForce {
    GTC("gtc"),
    IOC("ioc");

    public final String time;

    TimeInForce(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return this.time;
    }
}
