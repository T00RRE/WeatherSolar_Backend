package com.weathersolar.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.weathersolar.Model.DailyWeather;
import com.weathersolar.client.OpenMeteoClient;
import com.weathersolar.dto.WeatherForecastResponse;
import com.weathersolar.dto.WeeklySummaryResponse;
import com.weathersolar.exception.ExternalServiceException;
import com.weathersolar.exception.LocationValidationException;
import com.weathersolar.exception.WeatherDataProcessingException;
import com.weathersolar.utils.SolarEnergyCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private final OpenMeteoClient meteoClient;
    private JsonNode pressureData;

    public WeatherForecastResponse getForecast(double latitude, double longitude) {
        validateLocation(latitude, longitude);
        
        try {
            log.info("Fetching forecast for lat: {}, lon: {}", latitude, longitude);
            
            JsonNode weatherData = fetchWeatherData(latitude, longitude);
            this.pressureData = fetchPressureData(latitude, longitude);
            
            List<DailyWeather> dailyForecasts = processDailyWeather(weatherData);
            return createWeatherForecastResponse(dailyForecasts);
            
        } catch (WebClientResponseException e) {
            log.error("API response error for lat: {} lon: {} - Status: {}", latitude, longitude, e.getStatusCode());
            throw new ExternalServiceException(
                "Weather API returned error: " + e.getStatusText(), 
                e, 
                e.getStatusCode().value()
            );
        } catch (WebClientException e) {
            log.error("Network error fetching forecast for lat: {} lon: {}", latitude, longitude, e);
            throw new ExternalServiceException(
                "Network error while fetching weather data", 
                e, 
                503
            );
        } catch (WeatherDataProcessingException | LocationValidationException e) {
            // Re-throw specific exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching forecast for lat: {} lon: {}", latitude, longitude, e);
            throw new WeatherDataProcessingException(
                "Failed to fetch weather forecast: " + e.getMessage(), 
                e
            );
        }
    }
    
    private void validateLocation(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new LocationValidationException(
                "Invalid latitude: " + latitude + ". Must be between -90 and 90"
            );
        }
        if (longitude < -180 || longitude > 180) {
            throw new LocationValidationException(
                "Invalid longitude: " + longitude + ". Must be between -180 and 180"
            );
        }
    }
    
    private JsonNode fetchWeatherData(double latitude, double longitude) {
        JsonNode weatherData = meteoClient.getWeatherForecast(latitude, longitude)
            .block(Duration.ofSeconds(10));
            
        if (weatherData == null || !weatherData.has("daily")) {
            log.error("Invalid weather data received from API");
            throw new ExternalServiceException(
                "Invalid weather data received from API", 
                500
            );
        }
        
        return weatherData;
    }
    
    private JsonNode fetchPressureData(double latitude, double longitude) {
        JsonNode pressureData = meteoClient.getPressureData(latitude, longitude)
            .block(Duration.ofSeconds(10));
            
        if (pressureData == null || !pressureData.has("hourly")) {
            log.error("Invalid pressure data received from API");
            throw new ExternalServiceException(
                "Invalid pressure data received from API", 
                500
            );
        }
        
        return pressureData;
    }

    private List<DailyWeather> processDailyWeather(JsonNode weatherData) {
        try {
            JsonNode daily = weatherData.get("daily");

            if (!hasRequiredFields(daily)) {
                throw new WeatherDataProcessingException("Missing required weather data fields");
            }

            return IntStream.range(0, 7)
                .mapToObj(i -> processSingleDay(daily, i))
                .toList();
                
        } catch (Exception e) {
            log.error("Error processing weather data: {}", e.getMessage());
            throw new WeatherDataProcessingException("Failed to process weather data", e);
        }
    }
    
    private boolean hasRequiredFields(JsonNode daily) {
        return daily.has("time") && 
               daily.has("weathercode") && 
               daily.has("temperature_2m_max") && 
               daily.has("temperature_2m_min") &&
               daily.has("sunrise") && 
               daily.has("sunset") &&
               daily.has("daylight_duration");
    }
    
    private DailyWeather processSingleDay(JsonNode daily, int dayIndex) {
        try {
            LocalDate date = LocalDate.parse(daily.get("time").get(dayIndex).asText());
            int weatherCode = daily.get("weathercode").get(dayIndex).asInt();
            double maxTemp = daily.get("temperature_2m_max").get(dayIndex).asDouble();
            double minTemp = daily.get("temperature_2m_min").get(dayIndex).asDouble();
            
            double daylightHours = daily.get("daylight_duration").get(dayIndex).asDouble() / 3600;

            LocalTime sunrise = LocalTime.parse(daily.get("sunrise").get(dayIndex).asText().split("T")[1]);
            LocalTime sunset = LocalTime.parse(daily.get("sunset").get(dayIndex).asText().split("T")[1]);
            
            double sunExposureHours = calculateSunExposureHours(sunrise, sunset);
            double solarEnergy = SolarEnergyCalculator.calculateDailySolarEnergy(sunExposureHours);

            return DailyWeather.builder()
                .date(date)
                .weatherCode(weatherCode)
                .minTemperature(minTemp)
                .maxTemperature(maxTemp)
                .solarEnergy(solarEnergy)
                .daylightHours(daylightHours)
                .build();
                
        } catch (Exception e) {
            log.error("Error processing weather data for day {}: {}", dayIndex, e.getMessage());
            throw new WeatherDataProcessingException(
                "Error processing daily weather data for day " + dayIndex, 
                e
            );
        }
    }

    private double calculateSunExposureHours(LocalTime sunrise, LocalTime sunset) {
        try {
            return Duration.between(sunrise, sunset).toHours();
        } catch (Exception e) {
            log.error("Error calculating sun exposure hours: {}", e.getMessage());
            throw new WeatherDataProcessingException("Error calculating sun exposure", e);
        }
    }

    private WeatherForecastResponse createWeatherForecastResponse(List<DailyWeather> dailyForecasts) {
        try {
            double avgPressure = calculateAveragePressure();
            double avgSunExposure = calculateAverageSunExposure(dailyForecasts);
            double minTemp = findMinTemperature(dailyForecasts);
            double maxTemp = findMaxTemperature(dailyForecasts);
            String weatherSummary = generateWeatherSummary(dailyForecasts);

            return WeatherForecastResponse.builder()
                .dailyForecasts(dailyForecasts)
                .averagePressure(avgPressure)
                .averageSunExposure(avgSunExposure)
                .minTemperature(minTemp)
                .maxTemperature(maxTemp)
                .weatherSummary(weatherSummary)
                .build();
                
        } catch (Exception e) {
            log.error("Error creating weather forecast response: {}", e.getMessage());
            throw new WeatherDataProcessingException("Failed to create weather forecast response", e);
        }
    }

    private double calculateAveragePressure() {
        try {
            JsonNode hourlyPressure = pressureData.get("hourly").get("pressure_msl");
            if (hourlyPressure == null) {
                throw new WeatherDataProcessingException("Missing pressure data");
            }

            return StreamSupport.stream(hourlyPressure.spliterator(), false)
                .mapToDouble(JsonNode::asDouble)
                .average()
                .orElseThrow(() -> new WeatherDataProcessingException("Error calculating average pressure"));
                
        } catch (WeatherDataProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating average pressure: {}", e.getMessage());
            throw new WeatherDataProcessingException("Failed to calculate average pressure", e);
        }
    }

    private double calculateAverageSunExposure(List<DailyWeather> dailyForecasts) {
        try {
            return dailyForecasts.stream()
                .mapToDouble(DailyWeather::getSolarEnergy)
                .average()
                .orElseThrow(() -> new WeatherDataProcessingException("Error calculating average sun exposure"));
                
        } catch (WeatherDataProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating average sun exposure: {}", e.getMessage());
            throw new WeatherDataProcessingException("Failed to calculate average sun exposure", e);
        }
    }

    private double findMinTemperature(List<DailyWeather> dailyForecasts) {
        try {
            return dailyForecasts.stream()
                .mapToDouble(DailyWeather::getMinTemperature)
                .min()
                .orElseThrow(() -> new WeatherDataProcessingException("Error finding minimum temperature"));
                
        } catch (WeatherDataProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding minimum temperature: {}", e.getMessage());
            throw new WeatherDataProcessingException("Failed to find minimum temperature", e);
        }
    }

    private double findMaxTemperature(List<DailyWeather> dailyForecasts) {
        try {
            return dailyForecasts.stream()
                .mapToDouble(DailyWeather::getMaxTemperature)
                .max()
                .orElseThrow(() -> new WeatherDataProcessingException("Error finding maximum temperature"));
                
        } catch (WeatherDataProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding maximum temperature: {}", e.getMessage());
            throw new WeatherDataProcessingException("Failed to find maximum temperature", e);
        }
    }

    private String generateWeatherSummary(List<DailyWeather> dailyForecasts) {
        try {
            long rainyDays = dailyForecasts.stream()
                .filter(day -> day.getWeatherCode() >= 50 && day.getWeatherCode() <= 69)
                .count();

            return rainyDays > 3 ? "Spodziewane opady w większości dni" : "Przeważnie bez opadów";
            
        } catch (Exception e) {
            log.error("Error generating weather summary: {}", e.getMessage());
            throw new WeatherDataProcessingException("Failed to generate weather summary", e);
        }
    }

    public WeeklySummaryResponse getWeeklySummary(double latitude, double longitude) {
        try {
            WeatherForecastResponse forecast = getForecast(latitude, longitude);
            
            return WeeklySummaryResponse.builder()
                .averagePressure(forecast.getAveragePressure())
                .averageSunExposure(forecast.getAverageSunExposure())
                .minTemperature(forecast.getMinTemperature())
                .maxTemperature(forecast.getMaxTemperature())
                .weatherSummary(forecast.getWeatherSummary())
                .build();
                
        } catch (LocationValidationException | WeatherDataProcessingException | ExternalServiceException e) {
            // Re-throw specific exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error getting weekly summary for lat: {} lon: {}", latitude, longitude, e);
            throw new WeatherDataProcessingException("Failed to get weekly summary", e);
        }
    }
}