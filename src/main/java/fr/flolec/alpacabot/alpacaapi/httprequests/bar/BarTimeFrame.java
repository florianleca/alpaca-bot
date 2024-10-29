package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

import lombok.Getter;

@Getter
public enum BarTimeFrame {

    MIN1("1Min"),
    MIN2("2Min"),
    MIN5("5Min"),
    MIN10("10Min"),
    MIN15("15Min"),
    MIN20("20Min"),
    MIN30("30Min"),
    MIN45("45Min"),
    HOUR1("1Hour"),
    HOUR2("2Hour"),
    HOUR3("3Hour"),
    HOUR4("4Hour"),
    HOUR6("6Hour"),
    HOUR12("12Hour"),
    DAY1("1Day"),
    WEEK1("1Week"),
    MONTH1("1Month"),
    MONTH2("2Month"),
    MONTH3("3Month"),
    MONTH4("4Month"),
    MONTH6("6Month"),
    MONTH12("12Month");

    private final String label;

    BarTimeFrame(String label) {
        this.label = label;
    }

    public static BarTimeFrame fromLabel(String label) {
        for (BarTimeFrame timeFrame : values()) {
            if (timeFrame.getLabel().equalsIgnoreCase(label)) {
                return timeFrame;
            }
        }
        throw new IllegalArgumentException("No matching constant for " + label);
    }

}
