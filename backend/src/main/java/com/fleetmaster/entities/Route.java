package main.java.com.fleetmaster.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.HashSet;

@Entity
@Table(name = "route")
public class Route {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Please provide a starting location to this route")
    @Column(name = "startingLocation", nullable = false)
    private String startingLocation;

    @NotBlank(message = "Please provide a destination to this route")
    @Column(name = "destination", nullable = false)
    private String destination;

    @NotBlank(message = "Please provide a maximum travel time to this route.")
    @Min(value = 0.1, message = "The maximum travel time cannot be a negative number.")
    @Column(name = "maxTravelTime", nullable = false)
    private double maxTravelTime;

    @Column(name = "routeCheckpoints")
    private HashSet<RouteCheckpoint> routeCheckpoints = new HashSet<>();

    public String getStartingLocation() {
        return this.startingLocation;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getMaxTravelTime() {
        return this.maxTravelTime;
    }

    public void setMaxTravelTime(double maxTravelTime) {
        this.maxTravelTime = maxTravelTime;
    }

    public HashSet<RouteCheckpoint> getRouteCheckpoints() {
        return this.routeCheckpoints;
    }

    public void addRouteCheckpoint(RouteCheckpoint routeCheckpoint) {
        if (routeCheckpoint == null) {
            throw new IllegalArgumentException("Please provide a checkpoint");
        }

        this.routeCheckpoints.add(routeCheckpoint);
    }
}
