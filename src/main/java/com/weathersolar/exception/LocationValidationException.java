package com.weathersolar.exception;


public class LocationValidationException extends RuntimeException {
    public LocationValidationException(String message) {
        super(message);
    }
    
    public LocationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}