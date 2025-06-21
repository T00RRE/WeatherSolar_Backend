package com.weathersolar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.weathersolar.config.SolarProperties;
import com.weathersolar.utils.SolarEnergyCalculator;

class SolarEnergyCalculatorTest {
    private SolarEnergyCalculator calculator;

    @BeforeEach
    void setUp() {
        SolarProperties props = new SolarProperties();
        props.setPower(2.5);
        props.setPanelEfficiency(0.2);
        props.setSystemLosses(0.85);
        calculator = new SolarEnergyCalculator(props);
    }

    @Test
    void shouldCalculateEnergy() {
        double exposureHours = 12.0;
        double expectedEnergy = 2.5 * 12.0 * 0.2 * 0.85;
        double result = calculator.calculateDailySolarEnergy(exposureHours);
        assertEquals(expectedEnergy, result, 0.01);
    }

    @Test
    void shouldReturnZeroForZeroExposure() {
        double result = calculator.calculateDailySolarEnergy(0.0);
        assertEquals(0.0, result, 0.01);
    }

    @Test
    void shouldCalculateMonthlyEnergy() {
        double averageDailySunHours = 8.0;
        int daysInMonth = 30;
        double expectedDaily = calculator.calculateDailySolarEnergy(averageDailySunHours);
        double expectedMonthly = expectedDaily * daysInMonth;
        double result = calculator.calculateMonthlySolarEnergy(averageDailySunHours, daysInMonth);
        assertEquals(expectedMonthly, result, 0.01);
    }

    @Test
    void shouldCalculateYearlyEnergy() {
        double averageDailySunHours = 6.0;
        double expectedDaily = calculator.calculateDailySolarEnergy(averageDailySunHours);
        double expectedYearly = expectedDaily * 365;
        double result = calculator.calculateYearlySolarEnergy(averageDailySunHours);
        assertEquals(expectedYearly, result, 0.01);
    }

    @Test
    void shouldThrowExceptionForInvalidSunHours() {
        assertThrows(IllegalArgumentException.class, () ->
            calculator.calculateDailySolarEnergy(-1.0));
        assertThrows(IllegalArgumentException.class, () ->
            calculator.calculateDailySolarEnergy(25.0));
    }
}