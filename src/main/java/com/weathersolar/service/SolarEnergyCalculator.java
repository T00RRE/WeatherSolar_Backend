package com.weathersolar.service;

import org.springframework.stereotype.Service;

@Service
public class SolarEnergyCalculator {
    private static final double INSTALLATION_POWER = 2.5; // kW
    private static final double PANEL_EFFICIENCY = 0.2;

    public double calculateDailySolarEnergy(double sunExposureHours) {
        return INSTALLATION_POWER * sunExposureHours * PANEL_EFFICIENCY;
    }
}