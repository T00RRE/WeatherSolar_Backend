package com.weathersolar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LocationValidationException.class)
    public ResponseEntity<ErrorResponse> handleLocationValidation(LocationValidationException e) {
        log.warn("Location validation error: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_LOCATION", e.getMessage()));
    }

    @ExceptionHandler(WeatherDataProcessingException.class)
    public ResponseEntity<ErrorResponse> handleWeatherDataProcessing(WeatherDataProcessingException e) {
        log.error("Weather data processing error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse("DATA_PROCESSING_ERROR", e.getMessage()));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException e) {
        log.error("External service error: {} (status: {})", e.getMessage(), e.getStatusCode());
        
        HttpStatus status = e.getStatusCode() >= 500 ? 
            HttpStatus.BAD_GATEWAY : HttpStatus.SERVICE_UNAVAILABLE;
            
        return ResponseEntity.status(status)
            .body(new ErrorResponse("EXTERNAL_SERVICE_ERROR", 
                "Weather service temporarily unavailable"));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponse(WebClientResponseException e) {
        log.error("API client error: {} (status: {})", e.getMessage(), e.getStatusCode());
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse("API_ERROR", 
                "Unable to fetch weather data from external service"));
    }

    @ExceptionHandler(WebClientException.class)
    public ResponseEntity<ErrorResponse> handleWebClient(WebClientException e) {
        log.error("Network error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("NETWORK_ERROR", 
                "Network connection problem"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("Validation error: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", 
                "Invalid request parameters: " + e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("Method argument validation error: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
            
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("Missing required parameter: {}", e.getParameterName());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("MISSING_PARAMETER", 
                "Required parameter missing: " + e.getParameterName()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_ARGUMENT", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", 
                "An unexpected error occurred"));
    }


    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private long timestamp;

        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}