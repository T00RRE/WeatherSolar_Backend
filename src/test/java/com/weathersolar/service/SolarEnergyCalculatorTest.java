package com.weathersolar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SolarEnergyCalculatorTest {

    private final SolarEnergyCalculator calculator = new SolarEnergyCalculator();

    @Test
    void shouldCalculateEnergy() {
        // given
        double exposureHours = 12.0;
        double expectedEnergy = 2.5 * 12.0 * 0.2; // power * hours * efficiency

        // when
        double result = calculator.calculateDailySolarEnergy(exposureHours);

        // then
        assertEquals(expectedEnergy, result, 0.01);
    }

    @Test
    void shouldReturnZeroForZeroExposure() {
        assertEquals(0.0, calculator.calculateDailySolarEnergy(0.0), 0.01);
    }
}