package fr.flolec.alpacabot;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.flolec.alpacabot.alpacaapi.AlpacaApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlpacaApiException.class)
    public ResponseEntity<String> handleAlpacaApiException(AlpacaApiException e) {
        logger.error("{}: {} - {}", e.getCustomMessage(), e.getStatusCode(), e.getAlpacaErrorMessage());
        return ResponseEntity.status(e.getStatusCode()).body(e.getAlpacaErrorMessage());
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<String> handleJsonProcessingException(JsonProcessingException e) {
        logger.error("JsonProcessingException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(502)).body(e.getMessage());
    }

}
