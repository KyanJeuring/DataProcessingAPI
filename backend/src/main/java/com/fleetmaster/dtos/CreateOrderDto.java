package com.fleetmaster.dtos;

import java.time.LocalDateTime;

public class CreateOrderDto {
    private Long vehicleId;
    private Long driverId;
    private Double pickUpLat;
    private Double pickUpLon;
    private Double deliveryLat;
    private Double deliveryLon;
    private String loadType; // NORMAL, REFRIGERATED, HAZARDOUS
    private LocalDateTime departureTime;
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
