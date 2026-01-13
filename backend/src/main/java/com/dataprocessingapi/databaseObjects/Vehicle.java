package com.dataprocessingapi.databaseObjects;

import java.time.LocalDate;
import java.util.HashSet;

public class Vehicle  {
    private VehicleType vehicleType;
    private LoadType loadType;
    private double loadCapacity;
    private LocalDate manufactureYear;
    private double sensorData;
    private HashSet<LoadType> supportedLoadTypes;
    private HashSet<String> supportedRouteTypes;

    public Vehicle(LoadType loadType, VehicleType vehicleType, double loadCapacity,
                   LocalDate manufactureYear, double sensorData) {
        this.loadType = loadType;
        this.vehicleType = vehicleType;
        this.loadCapacity = loadCapacity;
        this.manufactureYear = manufactureYear;
        this.sensorData = sensorData;
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

    public double getLoadCapacity() {
        return this.loadCapacity;
    }

    public void setLoadCapacity(double loadCapacity) {
        this.loadCapacity = loadCapacity;
    }

    public LocalDate getManufactureYear() {
        return this.manufactureYear;
    }

    public void setManufactureYear(LocalDate manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public double getSensorData() {
        return this.sensorData;
    }

    public void setSensorData(double sensorData) {
        this.sensorData = sensorData;
    }

    public HashSet<LoadType> getSupportedLoadTypes() {
        return this.supportedLoadTypes;
    }

    public void setSupportedLoadTypes(HashSet<LoadType> supportedLoadTypes) {
        this.supportedLoadTypes = supportedLoadTypes;
    }

    public HashSet<String> getSupportedRouteTypes() {
        return this.supportedRouteTypes;
    }

    public void setSupportedRouteTypes(HashSet<String> supportedRouteTypes) {
        this.supportedRouteTypes = supportedRouteTypes;
    }
}
