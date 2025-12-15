package main.java.com.dataprocessingapi.databaseObjects;

public class WarehouseStock {
    private String name;
    private String location;

    public WarehouseStock(String name, String location) {
        this.setName(name);
        this.setLocation(location);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Please provide a warehouse stock name.");
        }

        this.name = name;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        if (location == null) {
            throw new IllegalArgumentException("Please provide a valid location.");
        }

        this.location = location;
    }
}
