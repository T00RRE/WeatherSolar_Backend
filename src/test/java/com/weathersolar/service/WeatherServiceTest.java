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

import reactor.core.publisher.Mono;

class WeatherServiceTest {

   @Mock
   private OpenMeteoClient meteoClient;

   @Mock
   private SolarEnergyCalculator energyCalculator;

   private WeatherService weatherService;
   private ObjectMapper objectMapper;

   @BeforeEach
   @SuppressWarnings("unused")
   void setUp() {
       MockitoAnnotations.openMocks(this);
       weatherService = new WeatherService(meteoClient, energyCalculator);
       objectMapper = new ObjectMapper();
   }

   @Test
   void shouldReturnWeatherForecast() throws Exception {
       // given
       String weatherJson = "{\"daily\":{\"time\":[\"2024-01-01\"],\"weathercode\":[1],\"temperature_2m_max\":[20.5],\"temperature_2m_min\":[10.5],\"sunrise\":[\"06:00\"],\"sunset\":[\"18:00\"]}}";
       String pressureJson = "{\"hourly\":{\"pressure_msl\":[1013.0,1014.0]}}";
       
       JsonNode weatherData = objectMapper.readTree(weatherJson);
       JsonNode pressureData = objectMapper.readTree(pressureJson);

       when(meteoClient.getWeatherForecast(anyDouble(), anyDouble()))
           .thenReturn(Mono.just(weatherData));
       when(meteoClient.getPressureData(anyDouble(), anyDouble()))
           .thenReturn(Mono.just(pressureData));
       when(energyCalculator.calculateDailySolarEnergy(anyDouble()))
           .thenReturn(5.0);

       // when
       WeatherForecastResponse response = (WeatherForecastResponse) weatherService.getForecast(52.0, 21.0);

       // then
       assertNotNull(response);
       assertFalse(response.getDailyForecasts().isEmpty());
       
       DailyWeather firstDay = response.getDailyForecasts().get(0);
       assertEquals(LocalDate.parse("2024-01-01"), firstDay.getDate());
       assertEquals(20.5, firstDay.getMaxTemperature());
       assertEquals(10.5, firstDay.getMinTemperature());
       assertEquals(5.0, firstDay.getSolarEnergy());
   }

   @Test
   void shouldCalculateAverages() throws Exception {
       // given
       String weatherJson = "{\"daily\":{\"time\":[\"2024-01-01\",\"2024-01-02\"],\"weathercode\":[1,1],\"temperature_2m_max\":[20.5,21.5],\"temperature_2m_min\":[10.5,11.5],\"sunrise\":[\"06:00\",\"06:00\"],\"sunset\":[\"18:00\",\"18:00\"]}}";
       String pressureJson = "{\"hourly\":{\"pressure_msl\":[1013.0,1014.0]}}";
       
       JsonNode weatherData = objectMapper.readTree(weatherJson);
       JsonNode pressureData = objectMapper.readTree(pressureJson);

       when(meteoClient.getWeatherForecast(anyDouble(), anyDouble()))
           .thenReturn(Mono.just(weatherData));
       when(meteoClient.getPressureData(anyDouble(), anyDouble()))
           .thenReturn(Mono.just(pressureData));
       when(energyCalculator.calculateDailySolarEnergy(anyDouble()))
           .thenReturn(5.0);

       // when
       WeatherForecastResponse response = (WeatherForecastResponse) weatherService.getForecast(52.0, 21.0);

       // then
       assertNotNull(response);
       assertEquals(1013.5, response.getAveragePressure(), 0.1);
       assertEquals(5.0, response.getAverageSunExposure(), 0.1);
       assertEquals(10.5, response.getMinTemperature());
       assertEquals(21.5, response.getMaxTemperature());
   }
}