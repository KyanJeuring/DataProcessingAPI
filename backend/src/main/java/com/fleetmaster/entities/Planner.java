package main.java.com.fleetmaster.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;

@Entity
@Table(name = "planner")
public class Planner {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "routes", nullable)
    private ArrayList<Route> routes = new ArrayList<>();

    public ArrayList<Route> getRoutes() {
        return this.routes;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void addRoute(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("Please provide a route to the planner.");
        }

        this.routes.add(route);
    }
}