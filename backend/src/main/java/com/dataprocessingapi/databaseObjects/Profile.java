package com.dataprocessingapi.databaseObjects;

public class Profile {
    private String name;
    private Employee role;
    private String location;

    public Profile(String name, Employee role, String location) {
        this.name = name;
        this.role = role;
        this.location = location;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee getRole() {
        return this.role;
    }

    public void setRole(Employee role) {
        this.role = role;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
