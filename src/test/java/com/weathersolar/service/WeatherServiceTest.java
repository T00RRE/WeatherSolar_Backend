package com.weathersolar.service;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyDouble;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weathersolar.Model.DailyWeather;
import com.weathersolar.client.OpenMeteoClient;
import com.weathersolar.dto.WeatherForecastResponse;
import com.weathersolar.utils.SolarEnergyCalculator;

import reactor.core.publisher.Mono;

class WeatherServiceTest {

   @Mock
   private OpenMeteoClient meteoClient;
   @Mock
   private SolarEnergyCalculator solarEnergyCalculator;

   private WeatherService weatherService;
   private ObjectMapper objectMapper;

   @BeforeEach
   void setUp() {
       MockitoAnnotations.openMocks(this);
       weatherService = new WeatherService(meteoClient, solarEnergyCalculator);
       objectMapper = new ObjectMapper();
       // domyślne mockowanie energii słonecznej
       when(solarEnergyCalculator.calculateDailySolarEnergy(anyDouble())).thenReturn(10.0);
   }

   @Test
   void shouldReturnWeatherForecast() throws Exception {
       String weatherJson = """
           {
               "daily": {
                   "time": ["2024-01-01"],
                   "weathercode": [1],
                   "temperature_2m_max": [20.5],
                   "temperature_2m_min": [10.5],
                   "sunrise": ["2024-01-01T06:00"],
                   "sunset": ["2024-01-01T18:00"],
                   "daylight_duration": [43200]
               }
           }
           """;
       
       String pressureJson = """
           {
               "hourly": {
                   "pressure_msl": [1013.0, 1014.0]
               }
           }
           """;
       
       JsonNode weatherData = objectMapper.readTree(weatherJson);
       JsonNode pressureData = objectMapper.readTree(pressureJson);

       when(meteoClient.getWeatherForecast(anyDouble(), anyDouble()))
           .thenReturn(Mono.just(weatherData));
       when(meteoClient.getPressureData(anyDouble(), anyDouble()))
           .thenReturn(Mono.just(pressureData));

       WeatherForecastResponse response = weatherService.getForecast(52.0, 21.0);

       assertNotNull(response);
       assertFalse(response.getDailyForecasts().isEmpty());
       
       DailyWeather firstDay = response.getDailyForecasts().get(0);
       assertEquals(LocalDate.parse("2024-01-01"), firstDay.getDate());
       assertEquals(20.5, firstDay.getMaxTemperature());
       assertEquals(10.5, firstDay.getMinTemperature());
       assertEquals(1, firstDay.getWeatherCode());
   }

   @Test
   void shouldCalculateAverages() throws Exception {
       String weatherJson = """
           {
               "daily": {
                   "time": ["2024-01-01", "2024-01-02"],
                   "weathercode": [1, 1],
                   "temperature_2m_max": [20.5, 21.5],
                   "temperature_2m_min": [10.5, 11.5],
                   "sunrise": ["2024-01-01T06:00", "2024-01-02T06:00"],
                   "sunset": ["2024-01-01T18:00", "2024-01-02T18:00"],
                   "daylight_duration": [43200, 43200]
               }
           }
           """;
       
       String pressureJson = """
           {
               "hourly": {
                   "pressure_msl": [1013.0, 1014.0]
               }
           }
           """;
       
       JsonNode weatherData = objectMapper.readTree(weatherJson);
       JsonNode pressureData = objectMapper.readTree(pressureJson);

       when(meteoClient.getWeatherForecast(anyDouble(), anyDouble()))
           .thenReturn(Mono.just(weatherData));
       when(meteoClient.getPressureData(anyDouble(), anyDouble()))
           .thenReturn(Mono.just(pressureData));

       WeatherForecastResponse response = weatherService.getForecast(52.0, 21.0);

       assertNotNull(response);
       assertEquals(2, response.getDailyForecasts().size());
       assertEquals(1013.5, response.getAveragePressure(), 0.1);
       assertEquals(10.5, response.getMinTemperature());
       assertEquals(21.5, response.getMaxTemperature());
       assertNotNull(response.getWeatherSummary());
   }
}