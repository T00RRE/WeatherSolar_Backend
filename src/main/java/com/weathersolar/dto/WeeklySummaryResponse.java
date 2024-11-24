package com.weathersolar.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummaryResponse {
   private double averagePressure;
   private double averageSunExposure;
   private double minTemperature;
   private double maxTemperature;
   private String weatherDescription;
   private String weatherSummary;
}