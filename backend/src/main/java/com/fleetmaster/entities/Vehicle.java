package main.java.com.fleetmaster.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.HashSet;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "VAN|LORRY|REFRIGIRATED_TRUCK", message = "Please provide the vehicle type: VAN, LORRY or REFRIGIRATED_TRUCK.")
    @Column(name = "type", nullable = false)
    private VehicleType vehicleType;

    @Pattern(regexp = "NORMAL|REFRIGIRATED|HAZARDOUS", message = "Please provide a load type: NORMAL, REFRIGIRATED or HAZARDOUS.")
    @Column(name = "load_type", nullable = false)
    private LoadType loadType;

    @NotBlank(message = "Please provide the load capacity of this vehicle.")
    @Min(value = 0.1, message = "The capacity of the truck cannot be a negative value.")
    @Column(name = "load_capacity", nullable = false)
    private LoadCapacity loadCapacity;

    @NotBlank(message = "Please provide the manufacture year.")
    @Past(message = "Please provide a date in the past")
    @Column(name = "year_of_manufacture", nullable = false)
    private LocalDate manufactureYear;

    @Column(name = "supported_load_types", nullable = false)
    private HashSet<LoadType> supportedLoadTypes = new HashSet<>();

    @Column(name = "supported_route_types", nullable = false)
    private HashSet<String> supportedRouteTypes = new HashSet<>();

    @Column(name = "sensor_data")
    private HashSet<String> sensorData = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VehicleType getVehicleType() {
        return this.vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LoadType getLoadType() {
        return this.loadType;
    }

    public void setLoadType(LoadType loadType) {
        this.loadType = loadType;
    }

    public LoadCapacity getLoadCapacity() {
        return this.loadCapacity;
    }

    public void setLoadCapacity(LoadCapacity loadCapacity) {
        this.loadCapacity = loadCapacity;
    }

    public LocalDate getManufactureYear() {
        return this.manufactureYear;
    }

    public void setManufactureYear(LocalDate manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public HashSet<LoadType> getSupportedLoadTypes() {
        return this.supportedLoadTypes;
    }

    public void addSupportedLoadType(LoadType loadType) {
        if (loadType == null) {
            throw new IllegalArgumentException("Please provide a load type.");
        }

        this.supportedLoadTypes.add(loadType);
    }

    public HashSet<String> getSupportedRouteTypes() {
        return this.supportedRouteTypes;
    }

    public void addSupportedRouteType(String supportedRouteType) {
        if (supportedRouteType == null || supportedRouteType.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a supported route type.");
        }

        this.supportedRouteTypes.add(supportedRouteType);
    }

    public HashSet<String> getSensorData() {
        return this.sensorData;
    }

    public void addSensorData(String sensorData) {
        if (sensorData == null || sensorData.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide sensor data.");
        }

        this.sensorData.add(sensorData);
    }
}
