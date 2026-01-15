package com.fleetmaster.dtos;

import java.time.LocalDate;
import java.util.List;

public class CreateVehicleDto {
    private Long loadCapacity;
    private String type; // LORRY, VAN, REFRIGERATED_TRUCK
    private LocalDate yearOfManufacture;
    private List<String> loadTypes; // NORMAL, REFRIGERATED, HAZARDOUS
    private Long lastOdometer;

    // Getters and Setters
    public Long getLoadCapacity() { return loadCapacity; }
    public void setLoadCapacity(Long loadCapacity) { this.loadCapacity = loadCapacity; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getYearOfManufacture() { return yearOfManufacture; }
    public void setYearOfManufacture(LocalDate yearOfManufacture) { this.yearOfManufacture = yearOfManufacture; }
    public List<String> getLoadTypes() { return loadTypes; }
    public void setLoadTypes(List<String> loadTypes) { this.loadTypes = loadTypes; }
    public Long getLastOdometer() { return lastOdometer; }
    public void setLastOdometer(Long lastOdometer) { this.lastOdometer = lastOdometer; }
}
