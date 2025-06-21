package com.weathersolar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.weathersolar.utils.SolarEnergyCalculator;

class SolarEnergyCalculatorTest {

    @Test
    void shouldCalculateEnergy() {
        // given
        double exposureHours = 12.0;
        // power * hours * efficiency * losses = 2.5 * 12.0 * 0.2 * 0.85
        double expectedEnergy = 2.5 * 12.0 * 0.2 * 0.85;

        // when
        double result = SolarEnergyCalculator.calculateDailySolarEnergy(exposureHours);

        // then
        assertEquals(expectedEnergy, result, 0.01);
    }

    @Test
    void shouldReturnZeroForZeroExposure() {
        // when
        double result = SolarEnergyCalculator.calculateDailySolarEnergy(0.0);
        
        // then
        assertEquals(0.0, result, 0.01);
    }

    @Test
    void shouldCalculateMonthlyEnergy() {
        // given
        double averageDailySunHours = 8.0;
        int daysInMonth = 30;
        double expectedDaily = SolarEnergyCalculator.calculateDailySolarEnergy(averageDailySunHours);
        double expectedMonthly = expectedDaily * daysInMonth;

        // when
        double result = SolarEnergyCalculator.calculateMonthlySolarEnergy(averageDailySunHours, daysInMonth);

        // then
        assertEquals(expectedMonthly, result, 0.01);
    }

    @Test
    void shouldCalculateYearlyEnergy() {
        // given
        double averageDailySunHours = 6.0;
        double expectedDaily = SolarEnergyCalculator.calculateDailySolarEnergy(averageDailySunHours);
        double expectedYearly = expectedDaily * 365;

        // when
        double result = SolarEnergyCalculator.calculateYearlySolarEnergy(averageDailySunHours);

        // then
        assertEquals(expectedYearly, result, 0.01);
    }

    @Test
    void shouldThrowExceptionForInvalidSunHours() {
        // then
        assertThrows(IllegalArgumentException.class, () -> 
            SolarEnergyCalculator.calculateDailySolarEnergy(-1.0));
        
        assertThrows(IllegalArgumentException.class, () -> 
            SolarEnergyCalculator.calculateDailySolarEnergy(25.0));
    }

    @Test
    void shouldCalculateSystemEfficiency() {
        // given
        double sunExposureHours = 10.0;
        double cloudCoverPercentage = 50.0;

        // when
        double result = SolarEnergyCalculator.calculateSystemEfficiency(sunExposureHours, cloudCoverPercentage);

        // then
        // Efficiency should be reduced due to cloud cover
        double expectedEfficiency = 0.2 * 0.85 * (1.0 - (50.0 / 100.0 * 0.7));
        assertEquals(expectedEfficiency, result, 0.01);
    }
}