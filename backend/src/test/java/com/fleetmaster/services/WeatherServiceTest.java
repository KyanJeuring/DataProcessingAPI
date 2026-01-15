package com.fleetmaster.services;

import com.fleetmaster.dtos.OpenMeteoResponse;
import com.fleetmaster.dtos.WeatherResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    private double testLat = 40.7128;
    private double testLon = -74.0060;

    @Test
    void testGetCurrentWeather_Success() {
        // Given
        OpenMeteoResponse mockResponse = new OpenMeteoResponse();
        OpenMeteoResponse.CurrentWeather current = new OpenMeteoResponse.CurrentWeather();
        current.setTemperature(22.5);
        current.setWeatherCode(0);
        current.setWindSpeed(5.5);
        mockResponse.setCurrent(current);

        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(mockResponse);

        // When
        WeatherResponseDto result = weatherService.getCurrentWeather(testLat, testLon);

        // Then
        assertNotNull(result);
        assertEquals(22.5, result.getTemperature());
        assertEquals("Clear sky", result.getCondition());
        assertEquals(5.5, result.getWindSpeed());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(OpenMeteoResponse.class));
    }

    @Test
    void testGetCurrentWeather_PartlyCloudy() {
        // Given
        OpenMeteoResponse mockResponse = new OpenMeteoResponse();
        OpenMeteoResponse.CurrentWeather current = new OpenMeteoResponse.CurrentWeather();
        current.setTemperature(18.0);
        current.setWeatherCode(2); // Partly cloudy
        current.setWindSpeed(3.2);
        mockResponse.setCurrent(current);

        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(mockResponse);

        // When
        WeatherResponseDto result = weatherService.getCurrentWeather(testLat, testLon);

        // Then
        assertNotNull(result);
        assertEquals("Partly cloudy", result.getCondition());
    }

    @Test
    void testGetCurrentWeather_Rain() {
        // Given
        OpenMeteoResponse mockResponse = new OpenMeteoResponse();
        OpenMeteoResponse.CurrentWeather current = new OpenMeteoResponse.CurrentWeather();
        current.setTemperature(15.0);
        current.setWeatherCode(61); // Rain
        current.setWindSpeed(8.0);
        mockResponse.setCurrent(current);

        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(mockResponse);

        // When
        WeatherResponseDto result = weatherService.getCurrentWeather(testLat, testLon);

        // Then
        assertNotNull(result);
        assertEquals("Rain", result.getCondition());
    }

    @Test
    void testGetCurrentWeather_Thunderstorm() {
        // Given
        OpenMeteoResponse mockResponse = new OpenMeteoResponse();
        OpenMeteoResponse.CurrentWeather current = new OpenMeteoResponse.CurrentWeather();
        current.setTemperature(20.0);
        current.setWeatherCode(95); // Thunderstorm
        current.setWindSpeed(12.0);
        mockResponse.setCurrent(current);

        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(mockResponse);

        // When
        WeatherResponseDto result = weatherService.getCurrentWeather(testLat, testLon);

        // Then
        assertNotNull(result);
        assertEquals("Thunderstorm", result.getCondition());
    }

    @Test
    void testGetCurrentWeather_NullResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            weatherService.getCurrentWeather(testLat, testLon);
        });

        assertEquals("Unable to fetch weather data", exception.getMessage());
    }

    @Test
    void testGetCurrentWeather_NullCurrent() {
        // Given
        OpenMeteoResponse mockResponse = new OpenMeteoResponse();
        mockResponse.setCurrent(null);

        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(mockResponse);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            weatherService.getCurrentWeather(testLat, testLon);
        });

        assertEquals("Unable to fetch weather data", exception.getMessage());
    }
}
