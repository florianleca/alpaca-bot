package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

public enum BarTimeFrame {

    MIN1("1Min"),
    MIN2("2Min"),
    MIN3("3Min"),
    MIN4("4Min"),
    MIN5("5Min"),
    MIN6("6Min"),
    MIN7("7Min"),
    MIN8("8Min"),
    MIN9("9Min"),
    MIN10("10Min"),
    MIN11("11Min"),
    MIN12("12Min"),
    MIN13("13Min"),
    MIN14("14Min"),
    MIN15("15Min"),
    MIN16("16Min"),
    MIN17("17Min"),
    MIN18("18Min"),
    MIN19("19Min"),
    MIN20("20Min"),
    MIN21("21Min"),
    MIN22("22Min"),
    MIN23("23Min"),
    MIN24("24Min"),
    MIN25("25Min"),
    MIN26("26Min"),
    MIN27("27Min"),
    MIN28("28Min"),
    MIN29("29Min"),
    MIN30("30Min"),
    MIN31("31Min"),
    MIN32("32Min"),
    MIN33("33Min"),
    MIN34("34Min"),
    MIN35("35Min"),
    MIN36("36Min"),
    MIN37("37Min"),
    MIN38("38Min"),
    MIN39("39Min"),
    MIN40("40Min"),
    MIN41("41Min"),
    MIN42("42Min"),
    MIN43("43Min"),
    MIN44("44Min"),
    MIN45("45Min"),
    MIN46("46Min"),
    MIN47("47Min"),
    MIN48("48Min"),
    MIN49("49Min"),
    MIN50("50Min"),
    MIN51("51Min"),
    MIN52("52Min"),
    MIN53("53Min"),
    MIN54("54Min"),
    MIN55("55Min"),
    MIN56("56Min"),
    MIN57("57Min"),
    MIN58("58Min"),
    MIN59("59Min"),
    HOUR1("1Hour"),
    HOUR2("2Hour"),
    HOUR3("3Hour"),
    HOUR4("4Hour"),
    HOUR5("5Hour"),
    HOUR6("6Hour"),
    HOUR7("7Hour"),
    HOUR8("8Hour"),
    HOUR9("9Hour"),
    HOUR10("10Hour"),
    HOUR11("11Hour"),
    HOUR12("12Hour"),
    HOUR13("13Hour"),
    HOUR14("14Hour"),
    HOUR15("15Hour"),
    HOUR16("16Hour"),
    HOUR17("17Hour"),
    HOUR18("18Hour"),
    HOUR19("19Hour"),
    HOUR20("20Hour"),
    HOUR21("21Hour"),
    HOUR22("22Hour"),
    HOUR23("23Hour"),
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

    public String getLabel() {
        return this.label;
    }

}
