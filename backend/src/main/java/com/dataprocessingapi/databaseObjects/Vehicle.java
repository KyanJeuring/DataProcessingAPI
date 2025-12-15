package main.java.com.dataprocessingapi.databaseObjects;

import java.time.LocalDate;
import java.util.HashSet;

public class Vehicle  {
    private static final double MAXIMUM_LOAD_CAPACITY = 40000.0;

    private VehicleType vehicleType;
    private LoadType loadType;
    private double loadCapacity;
    private LocalDate manufactureYear;
    private String sensorData;
    private HashSet<LoadType> supportedLoadTypes;
    private HashSet<String> supportedRouteTypes;

    public Vehicle(LoadType loadType, VehicleType vehicleType, double loadCapacity,
                   LocalDate manufactureYear, String sensorData) {
        this.setLoadType(loadType);
        this.setVehicleType(vehicleType);
        this.setLoadCapacity(loadCapacity);
        this.setManufactureYear(manufactureYear);
        this.setSensorData(sensorData);
        this.supportedLoadTypes = new HashSet<>();
        this.supportedRouteTypes = new HashSet<>();
    }

    public VehicleType getVehicleType() {
        return this.vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new IllegalArgumentException("Please provide a valid vehicle type.");
        }

        this.vehicleType = vehicleType;
    }

    public LoadType getLoadType() {
        return this.loadType;
    }

    public void setLoadType(LoadType loadType) {
        if (loadType == null) {
            throw new IllegalArgumentException("Please provide a valid load type.");
        }

        this.loadType = loadType;
    }

    public double getLoadCapacity() {
        return this.loadCapacity;
    }

    public void setLoadCapacity(double loadCapacity) {
        if (loadCapacity < 0) {
            throw new IllegalArgumentException("The load capacity cannot be a positive value.");
        }

        if (loadCapacity > MAXIMUM_LOAD_CAPACITY) {
            throw new IllegalArgumentException("This value is above the weight limit of " +
                                                MAXIMUM_LOAD_CAPACITY + ".");
        }

        this.loadCapacity = loadCapacity;
    }

    public int getManufactureYear() {
        return this.manufactureYear.getYear();
    }

    public void setManufactureYear(LocalDate manufactureYear) {
        if (this.manufactureYear == null) {
            throw new IllegalArgumentException("Please provide an valid date.");
        }

        if (this.manufactureYear.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The year of manufacture cannot be in the future.");
        }

        this.manufactureYear = manufactureYear;
    }

    public String getSensorData() {
        return this.sensorData;
    }

    public void setSensorData(String sensorData) {
        if (sensorData == null) {
            throw new IllegalArgumentException("Please provide a String.");
        }

        this.sensorData = sensorData;
    }

    public HashSet<LoadType> getSupportedLoadTypes() {
        return this.supportedLoadTypes;
    }

    public void addSupportedLoadType(LoadType loadType) {
        this.supportedLoadTypes.add(loadType);
    }

    public HashSet<String> getSupportedRouteTypes() {
        return this.supportedRouteTypes;
    }

    public void addSupportedRouteType(String string) {
        this.supportedRouteTypes.add(string);
    }
}
