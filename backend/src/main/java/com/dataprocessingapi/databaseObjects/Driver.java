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

    public void setVehicles(HashSet<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public HashSet<TransportOrder> getCurrentOrders() {
        return this.currentOrders;
    }

    public void setCurrentOrders(HashSet<TransportOrder> currentOrders) {
        this.currentOrders = currentOrders;
    }

    public HashSet<TransportOrder> getFutureOrders() {
        return this.futureOrders;
    }

    public void setFutureOrders(HashSet<TransportOrder> futureOrders) {
        this.futureOrders = futureOrders;
    }
}
