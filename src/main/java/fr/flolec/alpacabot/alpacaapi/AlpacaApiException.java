package fr.flolec.alpacabot.alpacaapi;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

@Getter
public class AlpacaApiException extends Exception {

    private final HttpStatusCode statusCode;
    private final String alpacaErrorMessage;
    private final String customMessage;

    public AlpacaApiException(HttpStatusCodeException e, String customMessage) {
        super(e);
        this.statusCode = e.getStatusCode();
        this.alpacaErrorMessage = e.getResponseBodyAsString();
        this.customMessage = customMessage;
    }

}
