package com.dataprocessingapi.databaseObjects;

import java.util.HashSet;

public class Route {
    private String startingLocation;
    private String destinationLocation;
    private double maxTravelTime;
    private HashSet<RouteCheckpoint> routeCheckpoints;

    public Route(String startingLocation, String destinationLocation, double maxTravelTime) {
        this.startingLocation = startingLocation;
        this.destinationLocation = destinationLocation;
        this.maxTravelTime = maxTravelTime;
    }

    public String getStartingLocation() {
        return this.startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }

    public String getDestinationLocation() {
        return this.destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
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

    public void setRouteCheckpoints(HashSet<RouteCheckpoint> routeCheckpoints) {
        this.routeCheckpoints = routeCheckpoints;
    }
}
