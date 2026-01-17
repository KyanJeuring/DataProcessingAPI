package com.fleetmaster.controllers;

import com.fleetmaster.dtos.WeatherResponseDto;
import com.fleetmaster.services.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    private WeatherResponseDto weatherResponse;

    @BeforeEach
    void setUp() {
        weatherResponse = new WeatherResponseDto();
        weatherResponse.setTemperature(22.5);
        weatherResponse.setWeatherDescription("Partly cloudy");
        weatherResponse.setHumidity(65);
        weatherResponse.setWindSpeed(15.0);
        weatherResponse.setLocation("Chicago");
    }

    @Test
    void testGetCurrentWeather_Success() throws Exception {
        // Given
        double lat = 41.8781;
        double lon = -87.6298;
        when(weatherService.getCurrentWeather(lat, lon)).thenReturn(weatherResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.temperature").value(22.5))
                .andExpect(jsonPath("$.weatherDescription").value("Partly cloudy"))
                .andExpect(jsonPath("$.humidity").value(65))
                .andExpect(jsonPath("$.windSpeed").value(15.0))
                .andExpect(jsonPath("$.location").value("Chicago"));
    }

    @Test
    void testGetCurrentWeather_XMLResponse() throws Exception {
        // Given
        double lat = 40.7128;
        double lon = -74.0060;
        when(weatherService.getCurrentWeather(lat, lon)).thenReturn(weatherResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon))
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }

    @Test
    void testGetCurrentWeather_NegativeCoordinates() throws Exception {
        // Given
        double lat = -33.8688;
        double lon = 151.2093;
        weatherResponse.setLocation("Sydney");
        when(weatherService.getCurrentWeather(lat, lon)).thenReturn(weatherResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Sydney"));
    }

    @Test
    void testGetCurrentWeather_ZeroCoordinates() throws Exception {
        // Given
        double lat = 0.0;
        double lon = 0.0;
        weatherResponse.setLocation("Gulf of Guinea");
        when(weatherService.getCurrentWeather(lat, lon)).thenReturn(weatherResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Gulf of Guinea"));
    }

    @Test
    void testGetCurrentWeather_ExtremeLatitude() throws Exception {
        // Given
        double lat = 78.2232;
        double lon = 15.6267;
        weatherResponse.setLocation("Svalbard");
        weatherResponse.setTemperature(-5.0);
        when(weatherService.getCurrentWeather(lat, lon)).thenReturn(weatherResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(-5.0))
                .andExpect(jsonPath("$.location").value("Svalbard"));
    }

    @Test
    void testGetCurrentWeather_MissingLatParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lon", "-87.6298")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCurrentWeather_MissingLonParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", "41.8781")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCurrentWeather_InvalidLatParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", "invalid")
                .param("lon", "-87.6298")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCurrentWeather_InvalidLonParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", "41.8781")
                .param("lon", "invalid")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCurrentWeather_HighPrecisionCoordinates() throws Exception {
        // Given
        double lat = 51.5074456;
        double lon = -0.1277583;
        weatherResponse.setLocation("London");
        when(weatherService.getCurrentWeather(lat, lon)).thenReturn(weatherResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("London"));
    }

    @Test
    void testGetCurrentWeather_MultipleConsecutiveRequests() throws Exception {
        // Given
        double lat1 = 40.7128;
        double lon1 = -74.0060;
        double lat2 = 34.0522;
        double lon2 = -118.2437;

        WeatherResponseDto response1 = new WeatherResponseDto();
        response1.setLocation("New York");
        response1.setTemperature(20.0);

        WeatherResponseDto response2 = new WeatherResponseDto();
        response2.setLocation("Los Angeles");
        response2.setTemperature(25.0);

        when(weatherService.getCurrentWeather(lat1, lon1)).thenReturn(response1);
        when(weatherService.getCurrentWeather(lat2, lon2)).thenReturn(response2);

        // When & Then - First request
        mockMvc.perform(get("/api/weather/current")
                .param("lat", String.valueOf(lat1))
                .param("lon", String.valueOf(lon1))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("New York"))
                .andExpect(jsonPath("$.temperature").value(20.0));

        // When & Then - Second request
        mockMvc.perform(get("/api/weather/current")
                .param("lat", String.valueOf(lat2))
                .param("lon", String.valueOf(lon2))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Los Angeles"))
                .andExpect(jsonPath("$.temperature").value(25.0));
    }
}
