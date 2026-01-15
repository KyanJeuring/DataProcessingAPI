package com.fleetmaster.dtos;

public class WeatherResponseDto {
    private Double temperature;
    private String condition;
    private Double windSpeed;

    public WeatherResponseDto(Double temperature, String condition, Double windSpeed) {
        this.temperature = temperature;
        this.condition = condition;
        this.windSpeed = windSpeed;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }
}
