// src/main/java/com/weathersolar/exception/GlobalExceptionHandler.java

package com.weathersolar.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientException.class)
    public ResponseEntity<String> handleWebClientException(WebClientException e) {
        return ResponseEntity.status(500)
            .body("Error fetching weather data: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.status(500)
            .body("An error occurred: " + e.getMessage());
    }
}