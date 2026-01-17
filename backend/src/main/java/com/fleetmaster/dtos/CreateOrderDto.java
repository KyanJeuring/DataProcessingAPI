package com.fleetmaster.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class CreateOrderDto {
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;
    
    @NotNull(message = "Driver ID is required")
    private Long driverId;
    
    @NotNull(message = "Pick up latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double pickUpLat;
    
    @NotNull(message = "Pick up longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double pickUpLon;
    
    @NotNull(message = "Delivery latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double deliveryLat;
    
    @NotNull(message = "Delivery longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double deliveryLon;
    
    @NotBlank(message = "Load type is required")
    @Pattern(regexp = "NORMAL|REFRIGERATED|HAZARDOUS", message = "Load type must be NORMAL, REFRIGERATED, or HAZARDOUS")
    private String loadType; // NORMAL, REFRIGERATED, HAZARDOUS
    
    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;
    
    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;

    // Getters and Setters
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public Double getPickUpLat() { return pickUpLat; }
    public void setPickUpLat(Double pickUpLat) { this.pickUpLat = pickUpLat; }
    public Double getPickUpLon() { return pickUpLon; }
    public void setPickUpLon(Double pickUpLon) { this.pickUpLon = pickUpLon; }
    public Double getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(Double deliveryLat) { this.deliveryLat = deliveryLat; }
    public Double getDeliveryLon() { return deliveryLon; }
    public void setDeliveryLon(Double deliveryLon) { this.deliveryLon = deliveryLon; }
    public String getLoadType() { return loadType; }
    public void setLoadType(String loadType) { this.loadType = loadType; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
}
