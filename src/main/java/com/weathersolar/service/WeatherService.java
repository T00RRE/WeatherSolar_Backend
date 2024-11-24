package com.weathersolar.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.weathersolar.Model.DailyWeather;
import com.weathersolar.client.OpenMeteoClient;
import com.weathersolar.dto.WeatherForecastResponse;
import com.weathersolar.dto.WeeklySummaryResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
   private final OpenMeteoClient meteoClient;
   private final SolarEnergyCalculator energyCalculator;
   private JsonNode pressureData;

   public WeatherForecastResponse getForecast(double latitude, double longitude) {
       try {
           log.info("Fetching forecast for lat: {}, lon: {}", latitude, longitude);
           
           JsonNode weatherData = meteoClient.getWeatherForecast(latitude, longitude)
               .block(Duration.ofSeconds(10));
           
           if (weatherData == null || !weatherData.has("daily")) {
               log.error("Invalid weather data received from API");
               throw new RuntimeException("Invalid weather data received from API");
           }

           this.pressureData = meteoClient.getPressureData(latitude, longitude)
               .block(Duration.ofSeconds(10));
           
           if (this.pressureData == null || !this.pressureData.has("hourly")) {
               log.error("Invalid pressure data received from API");
               throw new RuntimeException("Invalid pressure data received from API");
           }

           List<DailyWeather> dailyForecasts = processDailyWeather(weatherData);
           return createWeatherForecastResponse(dailyForecasts);
           
       } catch (Exception e) {
           log.error("Error fetching forecast for lat: {} lon: {}", latitude, longitude, e);
           throw new RuntimeException("Failed to fetch weather forecast: " + e.getMessage(), e);
       }
   }

   private List<DailyWeather> processDailyWeather(JsonNode weatherData) {
       try {
           List<DailyWeather> dailyForecasts = new ArrayList<>();
           JsonNode daily = weatherData.get("daily");

           if (!daily.has("time") || !daily.has("weathercode") || 
               !daily.has("temperature_2m_max") || !daily.has("temperature_2m_min") ||
               !daily.has("sunrise") || !daily.has("sunset")) {
               throw new RuntimeException("Missing required weather data fields");
           }

           for (int i = 0; i < 7; i++) {
               try {
                   LocalDate date = LocalDate.parse(daily.get("time").get(i).asText());
                   int weatherCode = daily.get("weathercode").get(i).asInt();
                   double maxTemp = daily.get("temperature_2m_max").get(i).asDouble();
                   double minTemp = daily.get("temperature_2m_min").get(i).asDouble();
                   
                   LocalTime sunrise = LocalTime.parse(daily.get("sunrise").get(i).asText().split("T")[1]);
                   LocalTime sunset = LocalTime.parse(daily.get("sunset").get(i).asText().split("T")[1]);
                   
                   double sunExposureHours = calculateSunExposureHours(sunrise, sunset);
                   double solarEnergy = energyCalculator.calculateDailySolarEnergy(sunExposureHours);

                   dailyForecasts.add(DailyWeather.builder()
                       .date(date)
                       .weatherCode(weatherCode)
                       .minTemperature(minTemp)
                       .maxTemperature(maxTemp)
                       .solarEnergy(solarEnergy)
                       .build());
                   
               } catch (Exception e) {
                   log.error("Error processing weather data for day {}: {}", i, e.getMessage());
                   throw new RuntimeException("Error processing daily weather data", e);
               }
           }

           return dailyForecasts;
           
       } catch (Exception e) {
           log.error("Error processing weather data: {}", e.getMessage());
           throw new RuntimeException("Failed to process weather data", e);
       }
   }

   private double calculateSunExposureHours(LocalTime sunrise, LocalTime sunset) {
       try {
           return Duration.between(sunrise, sunset).toHours();
       } catch (Exception e) {
           log.error("Error calculating sun exposure hours: {}", e.getMessage());
           throw new RuntimeException("Error calculating sun exposure", e);
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
           throw new RuntimeException("Failed to create weather forecast response", e);
       }
   }

   private double calculateAveragePressure() {
       try {
           JsonNode hourlyPressure = pressureData.get("hourly").get("pressure_msl");
           if (hourlyPressure == null) {
               throw new RuntimeException("Missing pressure data");
           }

           return StreamSupport.stream(hourlyPressure.spliterator(), false)
               .mapToDouble(JsonNode::asDouble)
               .average()
               .orElseThrow(() -> new RuntimeException("Error calculating average pressure"));
               
       } catch (Exception e) {
           log.error("Error calculating average pressure: {}", e.getMessage());
           throw new RuntimeException("Failed to calculate average pressure", e);
       }
   }

   private double calculateAverageSunExposure(List<DailyWeather> dailyForecasts) {
       try {
           return dailyForecasts.stream()
               .mapToDouble(DailyWeather::getSolarEnergy)
               .average()
               .orElseThrow(() -> new RuntimeException("Error calculating average sun exposure"));
               
       } catch (Exception e) {
           log.error("Error calculating average sun exposure: {}", e.getMessage());
           throw new RuntimeException("Failed to calculate average sun exposure", e);
       }
   }

   private double findMinTemperature(List<DailyWeather> dailyForecasts) {
       try {
           return dailyForecasts.stream()
               .mapToDouble(DailyWeather::getMinTemperature)
               .min()
               .orElseThrow(() -> new RuntimeException("Error finding minimum temperature"));
               
       } catch (Exception e) {
           log.error("Error finding minimum temperature: {}", e.getMessage());
           throw new RuntimeException("Failed to find minimum temperature", e);
       }
   }

   private double findMaxTemperature(List<DailyWeather> dailyForecasts) {
       try {
           return dailyForecasts.stream()
               .mapToDouble(DailyWeather::getMaxTemperature)
               .max()
               .orElseThrow(() -> new RuntimeException("Error finding maximum temperature"));
               
       } catch (Exception e) {
           log.error("Error finding maximum temperature: {}", e.getMessage());
           throw new RuntimeException("Failed to find maximum temperature", e);
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
           throw new RuntimeException("Failed to generate weather summary", e);
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
               
       } catch (Exception e) {
           log.error("Error getting weekly summary for lat: {} lon: {}", latitude, longitude, e);
           throw new RuntimeException("Failed to get weekly summary", e);
       }
   }
}