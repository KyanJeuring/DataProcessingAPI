package main.java.com.fleetmaster.entities;

import java.util.HashSet;

@Entity
@Table(name = "route")
public class Route {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "startingLocation", nullable = false)
    private String startingLocation;

    @Column(name = "destination", nullable = false)
    private String destination;

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
        if (id < 0) {
            throw new IllegalArgumentException("The ID cannot be a negative number.");
        }

        this.id = id;
    }

    public void setStartingLocation(String startingLocation) {
        if (startingLocation == null || startingLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a starting location.");
        }

        this.startingLocation = startingLocation;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a destination.");
        }

        this.destination = destination;
    }

    public double getMaxTravelTime() {
        return this.maxTravelTime;
    }

    public void setMaxTravelTime(double maxTravelTime) {
        if (maxTravelTime < 0.0) {
            throw new IllegalArgumentException("The maximum travel time cannot be a negative number.");
        }

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
