package main.java.com.fleetmaster.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_checkpoint")
public class RouteCheckpoint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Future(message = "The timestamp cannot be in the past.")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @NotBlank(message = "Please provide a location to this route checkpoint.")
    @Column(name = "location", nullable = false)
    private String location;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        if (id < 0) {
            throw new IllegalArgumentException("The ID cannot be a negative number.");
        }

        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
