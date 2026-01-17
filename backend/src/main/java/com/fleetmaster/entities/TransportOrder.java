package main.java.com.fleetmaster.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

@Entity
@Table(name = "transport_order")
public class TransportOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route", nullable = false)
    private Route route;

    @NotBlank(message = "Please provide the planned times.")
    @Future(message = "The planned times cannot be in the past.")
    @Column(name = "planned_times", nullable = false)
    private LocalDateTime plannedTimes;

    @NotBlank(message = "Please assign a driver to this transport order")
    @Column(name = "assigned_driver", nullable = false)
    private Driver assignedDriver;

    @Pattern(regexp = "PLANNED|IN_PROGRESS|INTERRUPTED|COMPLETED", message = "Please provide one of the following 
                                                                              statuses: PLANNED, IN_PROGRESS, 
                                                                              INTERRUPTED, COMPLETED.")
    @Column(name = "order_status", nullable = false)
    private OrderStatus OrderStatus;

    @Future(message = "The arrival time cannot be in the past.")
    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "route_progress")
    private ArrayList<RouteCheckpoint> routeProgress = new ArrayList<>();

    @NotBlank(message = "Please provide the departure of this transport order.")
    @Size(min = 2, max = 50, message = "The departure must have between 2 and 50 characters.")
    @Column(name = "departure", nullable = false)
    private String departure;

    @Column(name = "stopovers")
    private HashSet<RouteCheckpoint> stopOvers = new HashSet<>();

    @Future(message = "The breaks cannot be in the past.")
    @Column(name = "breaks")
    private LocalDateTime breaks;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Driver getAssignedDriver() {
        return this.assignedDriver;
    }

    public void setAssignedDriver(Driver assignedDriver) {
        this.assignedDriver = assignedDriver;
    }

    public OrderStatus getOrderStatus() {
        return this.OrderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.OrderStatus = orderStatus;
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

    public void addRouteProgress(RouteCheckpoint routeCheckpoint) {
        if (routeCheckpoint == null) {
            throw new IllegalArgumentException("Please provide a RouteCheckpoint object.");
        }

        this.routeProgress.add(routeCheckpoint);
    }

    public String getDeparture() {
        return this.departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public HashSet<RouteCheckpoint> getStopOvers() {
        return this.stopOvers;
    }

    public void addStopOver(HashSet<RouteCheckpoint> stopOver) {
        if (stopOver == null) {
            throw new IllegalArgumentException("Please provide a RouteCheckpoint object.");
        }
        
        this.stopOvers.add(stopOver);
    }

    public LocalDateTime getBreaks() {
        return this.breaks;
    }

    public void setBreaks(LocalDateTime breaks) {
        this.breaks = breaks;
    }
}
