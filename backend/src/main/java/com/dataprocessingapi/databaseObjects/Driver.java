package main.java.com.dataprocessingapi.databaseObjects;

import java.util.HashSet;

public class Driver {
    private HashSet<Vehicle> vehicles;
    private HashSet<TransportOrder> currentOrders;
    private HashSet<TransportOrder> futureOrders;

    public Driver() {

    }

    public HashSet<Vehicle> getVehicles() {
        return this.vehicles;
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Please provide a vehicle object.");
        }

        this.vehicles.add(vehicle);
    }

    public HashSet<TransportOrder> getCurrentOrders() {
        return this.currentOrders;
    }

    public void addCurrentOrder(TransportOrder currentOrder) {
        if (currentOrder == null) {
            throw new IllegalArgumentException("Please provide a transport order object.");
        }

        this.currentOrders.add(currentOrder);
    }

    public HashSet<TransportOrder> getFutureOrders() {
        return this.futureOrders;
    }

    public void addFutureOrder(TransportOrder futureOrder) {
        if (futureOrder == null) {
            throw new IllegalArgumentException("Please provide a transport order object.");
        }

        this.futureOrders.add(futureOrder);
    }
}
