package com.fleetmaster.dtos;

import java.util.Map;

public class AddProgressDto {
    private Long orderId;
    private Double lat;
    private Double lon;
    private String type;
    private Map<String, Object> description;

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getDescription() { return description; }
    public void setDescription(Map<String, Object> description) { this.description = description; }
}
