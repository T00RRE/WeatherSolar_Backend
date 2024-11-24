package com.weathersolar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weathersolar.dto.WeatherForecastResponse;
import com.weathersolar.dto.WeeklySummaryResponse;
import com.weathersolar.service.WeatherService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController {
   private final WeatherService weatherService;

   @GetMapping("/forecast")
   public ResponseEntity<WeatherForecastResponse> getWeatherForecast(
           @RequestParam @Min(-90) @Max(90) double latitude,
           @RequestParam @Min(-180) @Max(180) double longitude
   ) {
       return ResponseEntity.ok(weatherService.getForecast(latitude, longitude));
   }

   @GetMapping("/summary")
   public ResponseEntity<WeeklySummaryResponse> getWeeklySummary(
           @RequestParam @Min(-90) @Max(90) double latitude,
           @RequestParam @Min(-180) @Max(180) double longitude
   ) {
       return ResponseEntity.ok(weatherService.getWeeklySummary(latitude, longitude));
   }
}