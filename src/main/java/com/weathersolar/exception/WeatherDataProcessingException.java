package com.weathersolar.exception;


public class WeatherDataProcessingException extends RuntimeException {
    public WeatherDataProcessingException(String message) {
        super(message);
    }
    
    public WeatherDataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}