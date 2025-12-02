package main.java.com.dataprocessingapi.databaseObjects;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

public class TransportOrder {
    private Route route;
    private LocalDateTime plannedTimes;
    private Driver driver;
    private OrderStatus orderStatus;
    private LocalDateTime arrivalTime;
    private ArrayList<RouteCheckpoint> routeProgress;
    private String departure;
    private HashSet<String> stopovers;
    private HashSet<LocalDateTime> breaks;

    public TransportOrder(LocalDateTime plannedTimes, OrderStatus orderStatus,
                          LocalDateTime arrivalTime, String departure) {
        this.plannedTimes = plannedTimes;
        this.orderStatus = orderStatus;
        this.arrivalTime = arrivalTime;
        this.departure = departure;
    }

    public Route getRoute() {
        return this.route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public LocalDateTime getPlannedTimes() {
        return this.plannedTimes;
    }

    public void setPlannedTimes(LocalDateTime plannedTimes) {
        this.plannedTimes = plannedTimes;
    }

    public Driver getDriver() {
        return this.driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public OrderStatus getOrderStatus() {
        return this.orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getArrivalTime() {
        return this.arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public ArrayList<RouteCheckpoint> getRouteProgress() {
        return this.routeProgress;
    }

    public void setRouteProgress(ArrayList<RouteCheckpoint> routeProgress) {
        this.routeProgress = routeProgress;
    }

    public String getDeparture() {
        return this.departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public HashSet<String> getStopovers() {
        return this.stopovers;
    }

    public void setStopovers(HashSet<String> stopovers) {
        this.stopovers = stopovers;
    }

    public HashSet<LocalDateTime> getBreaks() {
        return this.breaks;
    }

    public void setBreaks(HashSet<LocalDateTime> breaks) {
        this.breaks = breaks;
    }
}
