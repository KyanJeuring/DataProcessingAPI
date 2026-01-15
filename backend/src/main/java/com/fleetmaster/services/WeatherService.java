package com.fleetmaster.services;

import com.fleetmaster.dtos.OpenMeteoResponse;
import com.fleetmaster.dtos.WeatherResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WeatherResponseDto getCurrentWeather(double lat, double lon) {
        String url = String.format(
            "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,weather_code,wind_speed_10m",
            lat, lon
        );

        OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);

        if (response != null && response.getCurrent() != null) {
            String condition = decodeWeatherCode(response.getCurrent().getWeatherCode());
            return new WeatherResponseDto(
                response.getCurrent().getTemperature(),
                condition,
                response.getCurrent().getWindSpeed()
            );
        }

        throw new RuntimeException("Unable to fetch weather data");
    }

    private String decodeWeatherCode(int code) {
        // Simple mapping of WMO weather codes
        if (code == 0) return "Clear sky";
        if (code >= 1 && code <= 3) return "Partly cloudy";
        if (code >= 45 && code <= 48) return "Fog";
        if (code >= 51 && code <= 55) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain showers";
        if (code >= 95 && code <= 99) return "Thunderstorm";
        return "Unknown";
    }
}
