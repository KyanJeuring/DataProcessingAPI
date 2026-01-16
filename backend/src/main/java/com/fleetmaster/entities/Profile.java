package main.java.com.fleetmaster.entities;

@Entity
@Table(name = "profile")
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "role" nullable = false)
    private Employee role;

    @Column(name = "location" nullable = false)
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a profile name.");
        }

        this.name = name;
    }

    public Employee getRole() {
        return this.role;
    }

    public void setRole(Employee role) {
        if (role == null) {
            throw new IllegalArgumentException("Please provide a role to this profile.");
        }

        this.role = role;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a location to this profile.");
        }

        this.location = location;
    }
}
