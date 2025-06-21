package com.weathersolar.controller;

import com.weathersolar.dto.WeatherForecastResponse;
import com.weathersolar.service.WeatherService;
import com.weathersolar.utils.SolarEnergyCalculator;
import com.weathersolar.client.OpenMeteoClient;
import com.weathersolar.config.SolarProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;
    @MockBean
    private SolarEnergyCalculator solarEnergyCalculator;
    @MockBean
    private OpenMeteoClient openMeteoClient;
    @MockBean
    private SolarProperties solarProperties;

    @Test
    @DisplayName("Should return 400 for invalid latitude/longitude")
    void shouldReturn400ForInvalidLatLng() throws Exception {
        // latitude < -90
        mockMvc.perform(get("/api/weather/forecast")
                .param("latitude", "-91")
                .param("longitude", "0"))
                .andExpect(status().isBadRequest());

        // latitude > 90
        mockMvc.perform(get("/api/weather/forecast")
                .param("latitude", "91")
                .param("longitude", "0"))
                .andExpect(status().isBadRequest());

        // longitude < -180
        mockMvc.perform(get("/api/weather/forecast")
                .param("latitude", "0")
                .param("longitude", "-181"))
                .andExpect(status().isBadRequest());

        // longitude > 180
        mockMvc.perform(get("/api/weather/forecast")
                .param("latitude", "0")
                .param("longitude", "181"))
                .andExpect(status().isBadRequest());
    }
}
