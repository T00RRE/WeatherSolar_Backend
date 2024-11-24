package com.weathersolar.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherForecast {
    private List<DailyWeather> dailyWeathers;
    private double averagePressure;
    private double averageSunExposure;
    private double minTemperature;
    private double maxTemperature;
    private String weatherSummary;
}