package com.weathersolar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "solar.installation")
public class SolarProperties {
    private double power;
    private double panelEfficiency;
    private double systemLosses;

    public double getPower() {
        return power;
    }
    public void setPower(double power) {
        this.power = power;
    }
    public double getPanelEfficiency() {
        return panelEfficiency;
    }
    public void setPanelEfficiency(double panelEfficiency) {
        this.panelEfficiency = panelEfficiency;
    }
    public double getSystemLosses() {
        return systemLosses;
    }
    public void setSystemLosses(double systemLosses) {
        this.systemLosses = systemLosses;
    }
}
