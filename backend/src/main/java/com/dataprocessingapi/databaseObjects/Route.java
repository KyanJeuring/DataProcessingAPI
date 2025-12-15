package main.java.com.dataprocessingapi.databaseObjects;

import java.util.HashSet;

public class Route {
    private String startingLocation;
    private String destinationLocation;
    private double maxTravelTime;
    private HashSet<RouteCheckpoint> routeCheckpoints;

    public Route(String startingLocation, String destinationLocation, double maxTravelTime) {
        this.setStartingLocation(startingLocation);
        this.setDestinationLocation(destinationLocation);
        this.setMaxTravelTime(maxTravelTime);
        this.routeCheckpoints = new HashSet<>();
    }

    public String getStartingLocation() {
        return this.startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        if (startingLocation == null) {
            throw new IllegalArgumentException("Please provide a starting location.");
        }

        this.startingLocation = startingLocation;
    }

    public String getDestinationLocation() {
        return this.destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        if (destinationLocation == null) {
            throw new IllegalArgumentException("Please provide a destination.");
        }

        this.destinationLocation = destinationLocation;
    }

    public double getMaxTravelTime() {
        return this.maxTravelTime;
    }

    public void setMaxTravelTime(double maxTravelTime) {
        if (maxTravelTime < 0) {
            throw new IllegalArgumentException("The travel time cannot be a negative number.");
        }

        this.maxTravelTime = maxTravelTime;
    }

    public HashSet<RouteCheckpoint> getRouteCheckpoints() {
        return this.routeCheckpoints;
    }

    public void addRouteCheckpoints(RouteCheckpoint routeCheckpoint) {
        if (routeCheckpoint == null) {
            throw new IllegalArgumentException("Please provide a valid route checkpoint.");
        }

        this.routeCheckpoints.add(routeCheckpoint);
    }
}
