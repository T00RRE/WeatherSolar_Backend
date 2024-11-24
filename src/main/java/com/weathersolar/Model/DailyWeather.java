package com.weathersolar.Model;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyWeather {
    private LocalDate date;
    private int weatherCode;
    private double minTemperature;
    private double maxTemperature;
    private double solarEnergy;
}