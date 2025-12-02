package main.java.com.dataprocessingapi.databaseObjects;

import java.time.LocalDate;

public class RouteCheckpoint {
    private LocalDate timestamp;
    private String location;

    public RouteCheckpoint(LocalDate timestamp, String location) {
        this.timestamp = timestamp;
        this.location = location;
    }

    public LocalDate getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
