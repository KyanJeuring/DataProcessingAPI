package main.java.com.dataprocessingapi.databaseObjects;

import java.util.ArrayList;

public class Planner {
    private ArrayList<Route> routes;

    public Planner() {
        this.routes = new ArrayList<>();
    }

    public ArrayList<Route> getRoutes() {
        return this.routes;
    }

    public void addRoute(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("Please provide a route object.");
        }

        this.routes.add(route);
    }
}
