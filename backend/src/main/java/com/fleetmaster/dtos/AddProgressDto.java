package com.fleetmaster.dtos;

import jakarta.validation.constraints.*;
import java.util.Map;

public class AddProgressDto {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double lat;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double lon;
    
    @NotBlank(message = "Progress type is required")
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
