package main.java.com.dataprocessingapi.databaseObjects;

public class Profile {
    private String name;
    private Employee role;
    private String location;

    public Profile(String name, Employee role, String location) {
        this.setName(name);
        this.setRole(role);
        this.setLocation(location);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Please provide a name.");
        }

        this.name = name;
    }

    public Employee getRole() {
        return this.role;
    }

    public void setRole(Employee role) {
        if (role == null) {
            throw new IllegalArgumentException("Please provide a role.");
        }

        this.setRole(role);
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        if (location == null) {
            throw new IllegalArgumentException("Please provide a location.");
        }

        this.location = location;
    }
}
