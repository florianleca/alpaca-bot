package fr.flolec.alpacabot.alpacaapi.httprequests.asset;

import lombok.Getter;

@Getter
public enum AssetClass {

    US_EQUITY("us_equity"),
    CRYPTO("crypto");

    private final String label;

    AssetClass(String label) {
        this.label = label;
    }

}
