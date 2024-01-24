package fr.flolec.alpacabot.alpacaapi.httprequests.bar;

public enum BarTimeFrame {

    _1MIN("1Min"),
    _2MIN("2Min"),
    _3MIN("3Min"),
    _4MIN("4Min"),
    _5MIN("5Min"),
    _6MIN("6Min"),
    _7MIN("7Min"),
    _8MIN("8Min"),
    _9MIN("9Min"),
    _10MIN("10Min"),
    _11MIN("11Min"),
    _12MIN("12Min"),
    _13MIN("13Min"),
    _14MIN("14Min"),
    _15MIN("15Min"),
    _16MIN("16Min"),
    _17MIN("17Min"),
    _18MIN("18Min"),
    _19MIN("19Min"),
    _20MIN("20Min"),
    _21MIN("21Min"),
    _22MIN("22Min"),
    _23MIN("23Min"),
    _24MIN("24Min"),
    _25MIN("25Min"),
    _26MIN("26Min"),
    _27MIN("27Min"),
    _28MIN("28Min"),
    _29MIN("29Min"),
    _30MIN("30Min"),
    _31MIN("31Min"),
    _32MIN("32Min"),
    _33MIN("33Min"),
    _34MIN("34Min"),
    _35MIN("35Min"),
    _36MIN("36Min"),
    _37MIN("37Min"),
    _38MIN("38Min"),
    _39MIN("39Min"),
    _40MIN("40Min"),
    _41MIN("41Min"),
    _42MIN("42Min"),
    _43MIN("43Min"),
    _44MIN("44Min"),
    _45MIN("45Min"),
    _46MIN("46Min"),
    _47MIN("47Min"),
    _48MIN("48Min"),
    _49MIN("49Min"),
    _50MIN("50Min"),
    _51MIN("51Min"),
    _52MIN("52Min"),
    _53MIN("53Min"),
    _54MIN("54Min"),
    _55MIN("55Min"),
    _56MIN("56Min"),
    _57MIN("57Min"),
    _58MIN("58Min"),
    _59MIN("59Min"),
    _1HOUR("1Hour"),
    _2HOUR("2Hour"),
    _3HOUR("3Hour"),
    _4HOUR("4Hour"),
    _5HOUR("5Hour"),
    _6HOUR("6Hour"),
    _7HOUR("7Hour"),
    _8HOUR("8Hour"),
    _9HOUR("9Hour"),
    _10HOUR("10Hour"),
    _11HOUR("11Hour"),
    _12HOUR("12Hour"),
    _13HOUR("13Hour"),
    _14HOUR("14Hour"),
    _15HOUR("15Hour"),
    _16HOUR("16Hour"),
    _17HOUR("17Hour"),
    _18HOUR("18Hour"),
    _19HOUR("19Hour"),
    _20HOUR("20Hour"),
    _21HOUR("21Hour"),
    _22HOUR("22Hour"),
    _23HOUR("23Hour"),
    _1DAY("1Day"),
    _1WEEK("1Week"),
    _1MONTH("1Month"),
    _2MONTH("2Month"),
    _3MONTH("3Month"),
    _4MONTH("4Month"),
    _6MONTH("6Month"),
    _12MONTH("12Month");

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

    private String getLabel() {
        return this.label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
