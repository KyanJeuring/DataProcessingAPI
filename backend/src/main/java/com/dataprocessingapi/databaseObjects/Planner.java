package com.dataprocessingapi.databaseObjects;

import java.util.ArrayList;

public class Planner {
    private ArrayList<Route> routes;

    public Planner() {

    }

    public ArrayList<Route> getRoutes() {
        return this.routes;
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }
}
