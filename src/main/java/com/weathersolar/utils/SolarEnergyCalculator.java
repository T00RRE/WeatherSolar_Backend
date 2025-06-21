package com.weathersolar.utils;

import com.weathersolar.config.SolarProperties;
import org.springframework.stereotype.Component;

@Component
public class SolarEnergyCalculator {
    private final SolarProperties solarProperties;

    public SolarEnergyCalculator(SolarProperties solarProperties) {
        this.solarProperties = solarProperties;
    }

    public double calculateDailySolarEnergy(double sunExposureHours) {
        validateSunExposureHours(sunExposureHours);
        return solarProperties.getPower() * sunExposureHours * solarProperties.getPanelEfficiency() * solarProperties.getSystemLosses();
    }

    public double calculateMonthlySolarEnergy(double averageDailySunHours, int daysInMonth) {
        validateSunExposureHours(averageDailySunHours);
        validateDaysInMonth(daysInMonth);
        return calculateDailySolarEnergy(averageDailySunHours) * daysInMonth;
    }

    public double calculateYearlySolarEnergy(double averageDailySunHours) {
        validateSunExposureHours(averageDailySunHours);
        return calculateDailySolarEnergy(averageDailySunHours) * 365;
    }

    public double calculateSystemEfficiency(double sunExposureHours, double cloudCoverPercentage) {
        validateSunExposureHours(sunExposureHours);
        validateCloudCover(cloudCoverPercentage);
        double cloudReduction = 1.0 - (cloudCoverPercentage / 100.0 * 0.7);
        return solarProperties.getPanelEfficiency() * solarProperties.getSystemLosses() * cloudReduction;
    }

    private void validateSunExposureHours(double sunExposureHours) {
        if (sunExposureHours < 0 || sunExposureHours > 24) {
            throw new IllegalArgumentException(
                "Sun exposure hours must be between 0 and 24, got: " + sunExposureHours
            );
        }
    }

    private void validateDaysInMonth(int daysInMonth) {
        if (daysInMonth < 1 || daysInMonth > 31) {
            throw new IllegalArgumentException(
                "Days in month must be between 1 and 31, got: " + daysInMonth
            );
        }
    }

    private void validateCloudCover(double cloudCoverPercentage) {
        if (cloudCoverPercentage < 0 || cloudCoverPercentage > 100) {
            throw new IllegalArgumentException(
                "Cloud cover percentage must be between 0 and 100, got: " + cloudCoverPercentage
            );
        }
    }
}