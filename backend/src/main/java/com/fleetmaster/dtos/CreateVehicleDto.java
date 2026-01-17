package com.fleetmaster.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public class CreateVehicleDto {
    @NotNull(message = "Load capacity is required")
    @Min(value = 0, message = "Load capacity cannot be negative")
    private Long loadCapacity;
    
    @NotBlank(message = "Vehicle type is required")
    @Pattern(regexp = "LORRY|VAN|REFRIGERATED_TRUCK", message = "Type must be LORRY, VAN, or REFRIGERATED_TRUCK")
    private String type; // LORRY, VAN, REFRIGERATED_TRUCK
    
    @NotNull(message = "Year of manufacture is required")
    @PastOrPresent(message = "Year of manufacture cannot be in the future")
    private LocalDate yearOfManufacture;
    
    @NotEmpty(message = "Load types are required")
    private List<String> loadTypes; // NORMAL, REFRIGERATED, HAZARDOUS
    
    @NotNull(message = "Last odometer reading is required")
    @Min(value = 0, message = "Odometer reading cannot be negative")
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
