package fr.flolec.alpacabot.alpacaapi.bar;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public enum PeriodLengthUnit {

    MIN("Min") {
        @Override
        public String goBackInTime(OffsetDateTime dateTime, long amount) {
            return dateTime.minusMinutes(amount).format(rfc3339Formatter);
        }
    },
    HOUR("Hour") {
        @Override
        public String goBackInTime(OffsetDateTime dateTime, long amount) {
            return dateTime.minusHours(amount).format(rfc3339Formatter);
        }
    },
    DAY("Day") {
        @Override
        public String goBackInTime(OffsetDateTime dateTime, long amount) {
            return dateTime.minusDays(amount).format(rfc3339Formatter);
        }
    },
    WEEK("Week") {
        @Override
        public String goBackInTime(OffsetDateTime dateTime, long amount) {
            return dateTime.minusWeeks(amount).format(rfc3339Formatter);
        }
    },
    MONTH("Month") {
        @Override
        public String goBackInTime(OffsetDateTime dateTime, long amount) {
            return dateTime.minusMonths(amount).format(rfc3339Formatter);
        }
    };

    public static final DateTimeFormatter rfc3339Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public final String label;

    PeriodLengthUnit(String label) {
        this.label = label;
    }

    public static PeriodLengthUnit fromLabel(String label) {
        for (PeriodLengthUnit unit : values()) {
            if (unit.getLabel().equalsIgnoreCase(label)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("No matching constant for " + label);
    }

    public abstract String goBackInTime(OffsetDateTime dateTime, long amount);

}
