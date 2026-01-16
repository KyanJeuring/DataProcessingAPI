package main.java.com.fleetmaster.entities;

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
        if (id < 0) {
            throw new IllegalArgumentException("The ID cannot be a negative number.");
        }

        this.id = id;
    }

    public void addRoute(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("Please provide a route to the planner.");
        }

        this.routes.add(route);
    }
}