package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

import lombok.Getter;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;

@Getter
public enum BarTimeFrame {

    MIN1("1Min", Duration.ofMinutes(1)),
    MIN2("2Min", Duration.ofMinutes(2)),
    MIN5("5Min", Duration.ofMinutes(5)),
    MIN10("10Min", Duration.ofMinutes(10)),
    MIN15("15Min", Duration.ofMinutes(15)),
    MIN20("20Min", Duration.ofMinutes(20)),
    MIN30("30Min", Duration.ofMinutes(30)),
    MIN45("45Min", Duration.ofMinutes(45)),
    HOUR1("1Hour", Duration.ofHours(1)),
    HOUR2("2Hour", Duration.ofHours(2)),
    HOUR3("3Hour", Duration.ofHours(3)),
    HOUR4("4Hour", Duration.ofHours(4)),
    HOUR6("6Hour", Duration.ofHours(6)),
    HOUR12("12Hour", Duration.ofHours(12)),
    DAY1("1Day", Period.ofDays(1)),
    WEEK1("1Week", Period.ofWeeks(1)),
    MONTH1("1Month", Period.ofMonths(1)),
    MONTH2("2Month", Period.ofMonths(2)),
    MONTH3("3Month", Period.ofMonths(3)),
    MONTH4("4Month", Period.ofMonths(4)),
    MONTH6("6Month", Period.ofMonths(6)),
    MONTH12("12Month", Period.ofMonths(12));

    private final String label;
    private final TemporalAmount temporalAmount;

    BarTimeFrame(String label, TemporalAmount temporalAmount) {
        this.label = label;
        this.temporalAmount = temporalAmount;
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
