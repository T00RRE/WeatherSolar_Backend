package com.weathersolar.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.weathersolar.service.WeatherService;

class WeatherControllerTest {

    private MockMvc mockMvc;
    private WeatherService weatherService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        weatherService = mock(WeatherService.class);
        WeatherController controller = new WeatherController(weatherService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnForecast() throws Exception {
        mockMvc.perform(get("/api/weather/forecast")
                .param("latitude", "52.0")
                .param("longitude", "21.0"))
            .andExpect(status().isOk());
    }
}