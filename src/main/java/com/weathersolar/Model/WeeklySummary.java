package com.weathersolar.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummary {
    private double averagePressure;
    private double averageSunExposure;
    private double minTemperature;
    private double maxTemperature;
    private String weatherDescription;
}