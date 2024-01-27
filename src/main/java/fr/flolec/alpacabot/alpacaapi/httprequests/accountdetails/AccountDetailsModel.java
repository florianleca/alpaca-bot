package fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Date;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDetailsModel {

    @JsonProperty("id")
    private String id;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("crypto_status")
    private String cryptoStatus;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("cash")
    private String cash;

    @JsonProperty("pending_transfer_in")
    private String pendingTransferIn;

    @JsonProperty("trading_blocked")
    private Boolean tradingBlocked;

    @JsonProperty("transfers_blocked")
    private Boolean transfersBlocked;

    @JsonProperty("account_blocked")
    private Boolean accountBlocked;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("equity")
    private String equity;

}
