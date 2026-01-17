package main.java.com.fleetmaster.entities;


@Entity
@Table(name = "warehouse_stock")
public class WarehouseStock {
    @NotBlank(message = "Please provide a name to this warehouse stock.")
    @Size(min = 2, max = 50, messsage = "The name must be between 2 and 50 characters long")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Please provide a location to this warehouse stock.")
    @Column(name = "Location", nullable = false)
    private String location;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
