package com.weathersolar.client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OpenMeteoClient {
    private final WebClient webClient;
    private static final String BASE_URL = "https://api.open-meteo.com/v1";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    public OpenMeteoClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl(BASE_URL)
            .build();
    }

    public Mono<JsonNode> getWeatherForecast(double latitude, double longitude) {
        String path = "/forecast";
        log.info("Fetching weather forecast from: {}{} for lat: {}, lon: {}", 
                BASE_URL, path, latitude, longitude);

        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("daily", "weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_hours,daylight_duration")
                .queryParam("timezone", "auto")  
                .queryParam("forecast_days", "7")
                .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnSuccess(response -> log.info("Successfully received weather forecast data"))
            .doOnError(error -> log.error("Error fetching weather forecast: {}", error.getMessage()))
            .onErrorResume(WebClientResponseException.class, e -> {
                log.error("API error response: {}", e.getResponseBodyAsString());
                return Mono.error(new RuntimeException("Failed to fetch weather data: " + e.getMessage()));
            });
    }

    public Mono<JsonNode> getPressureData(double latitude, double longitude) {
        String path = "/forecast";
        log.info("Fetching pressure data from: {}{} for lat: {}, lon: {}", 
                BASE_URL, path, latitude, longitude);

        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("hourly", "pressure_msl")
                .queryParam("timezone", "auto")  
                .queryParam("forecast_days", "7")
                .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnSuccess(response -> log.info("Successfully received pressure data"))
            .doOnError(error -> log.error("Error fetching pressure data: {}", error.getMessage()))
            .onErrorResume(WebClientResponseException.class, e -> {
                log.error("API error response: {}", e.getResponseBodyAsString());
                return Mono.error(new RuntimeException("Failed to fetch pressure data: " + e.getMessage()));
            });
    }

    public LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            log.error("Error parsing date: {} - {}", dateString, e.getMessage());
            throw new RuntimeException("Failed to parse date: " + dateString, e);
        }
    }
}