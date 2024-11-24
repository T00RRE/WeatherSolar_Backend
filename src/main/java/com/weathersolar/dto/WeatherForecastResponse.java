package com.weathersolar.dto;

import com.weathersolar.Model.DailyWeather;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherForecastResponse {
    private List<DailyWeather> dailyForecasts;
    private double averagePressure;
    private double averageSunExposure;
    private double minTemperature;
    private double maxTemperature;
    private String weatherSummary;
}