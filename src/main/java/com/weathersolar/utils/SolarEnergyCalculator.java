package com.weathersolar.utils;

/**
 * Klasa utilitarna do obliczania energii słonecznej
 * Używa wzorów fizycznych do szacowania produkcji energii z paneli słonecznych
 */
public final class SolarEnergyCalculator {
    
    // Stałe konfiguracyjne dla instalacji fotowoltaicznej
    private static final double INSTALLATION_POWER = 2.5; // kW - moc instalacji
    private static final double PANEL_EFFICIENCY = 0.2;   // 20% - sprawność paneli
    private static final double SYSTEM_LOSSES = 0.85;     // 15% straty systemowe
    
    // Prywatny konstruktor - zapobiega instancjonowaniu
    private SolarEnergyCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Oblicza dzienną produkcję energii słonecznej na podstawie godzin nasłonecznienia
     * 
     * @param sunExposureHours liczba godzin nasłonecznienia w danym dniu
     * @return szacowana produkcja energii w kWh
     * @throws IllegalArgumentException jeśli sunExposureHours < 0 lub > 24
     */
    public static double calculateDailySolarEnergy(double sunExposureHours) {
        validateSunExposureHours(sunExposureHours);
        
        return INSTALLATION_POWER * sunExposureHours * PANEL_EFFICIENCY * SYSTEM_LOSSES;
    }
    
    /**
     * Oblicza miesięczną produkcję energii słonecznej
     * 
     * @param averageDailySunHours średnia dzienna liczba godzin nasłonecznienia
     * @param daysInMonth liczba dni w miesiącu
     * @return szacowana miesięczna produkcja energii w kWh
     */
    public static double calculateMonthlySolarEnergy(double averageDailySunHours, int daysInMonth) {
        validateSunExposureHours(averageDailySunHours);
        validateDaysInMonth(daysInMonth);
        
        return calculateDailySolarEnergy(averageDailySunHours) * daysInMonth;
    }
    
    /**
     * Oblicza roczną produkcję energii słonecznej
     * 
     * @param averageDailySunHours średnia dzienna liczba godzin nasłonecznienia w roku
     * @return szacowana roczna produkcja energii w kWh
     */
    public static double calculateYearlySolarEnergy(double averageDailySunHours) {
        validateSunExposureHours(averageDailySunHours);
        
        return calculateDailySolarEnergy(averageDailySunHours) * 365;
    }
    
    /**
     * Oblicza sprawność systemu w danym dniu na podstawie warunków pogodowych
     * 
     * @param sunExposureHours liczba godzin nasłonecznienia
     * @param cloudCoverPercentage pokrycie chmurami w procentach (0-100)
     * @return skorygowana sprawność systemu
     */
    public static double calculateSystemEfficiency(double sunExposureHours, double cloudCoverPercentage) {
        validateSunExposureHours(sunExposureHours);
        validateCloudCover(cloudCoverPercentage);
        
        // Redukcja sprawności na podstawie pokrycia chmurami
        double cloudReduction = 1.0 - (cloudCoverPercentage / 100.0 * 0.7);
        return PANEL_EFFICIENCY * SYSTEM_LOSSES * cloudReduction;
    }
    
    // Metody walidacyjne
    private static void validateSunExposureHours(double sunExposureHours) {
        if (sunExposureHours < 0 || sunExposureHours > 24) {
            throw new IllegalArgumentException(
                "Sun exposure hours must be between 0 and 24, got: " + sunExposureHours
            );
        }
    }
    
    private static void validateDaysInMonth(int daysInMonth) {
        if (daysInMonth < 1 || daysInMonth > 31) {
            throw new IllegalArgumentException(
                "Days in month must be between 1 and 31, got: " + daysInMonth
            );
        }
    }
    
    private static void validateCloudCover(double cloudCoverPercentage) {
        if (cloudCoverPercentage < 0 || cloudCoverPercentage > 100) {
            throw new IllegalArgumentException(
                "Cloud cover percentage must be between 0 and 100, got: " + cloudCoverPercentage
            );
        }
    }
}