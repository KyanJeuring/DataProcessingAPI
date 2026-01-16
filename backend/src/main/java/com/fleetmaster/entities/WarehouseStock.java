package main.java.com.fleetmaster.entities;


@Entity
@Table(name = "warehouse_stock")
public class WarehouseStock {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "Location", nullable = false)
    private String location;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a name to this warehouse stock.");
        }

        this.name = name;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a location to this warehouse stock.");
        }
        
        this.location = location;
    }
}
