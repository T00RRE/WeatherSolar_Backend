package com.weathersolar.exception;

/**
 * Wyjątek rzucany gdy zewnętrzne API pogodowe nie odpowiada lub zwraca błędy
 */
public class ExternalServiceException extends RuntimeException {
    private final int statusCode;
    
    public ExternalServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public ExternalServiceException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}