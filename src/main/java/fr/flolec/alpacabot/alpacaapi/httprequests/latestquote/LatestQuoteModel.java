package fr.flolec.alpacabot.alpacaapi.httprequests.latestquote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
public class LatestQuoteModel {

    @JsonProperty("quotes")
    private Map<String, QuoteDetailsModel> quotes;

    @Getter
    @Setter
    public static class QuoteDetailsModel {

        /**
         * Ask rice (Prix de vente)
         * Prix minimum auquel quelqu'un est prêt à vendre sur le marché.
         * À regarder lorsqu'on souhaite acheter la crypto.
         */
        @JsonProperty("ap")
        private double askPrice;

        @JsonProperty("as")
        private double askSize;

        /**
         * Bid price (Prix d'achat)
         * Prix maximum auquel un acheteur est prêt à acheter.
         * À regarder lorsqu'on souhaite vendre la crypto.
         */
        @JsonProperty("bp")
        private double bidPrice;

        @JsonProperty("bs")
        private double bidSize;

        @JsonProperty("t")
        private ZonedDateTime time;

    }

}
