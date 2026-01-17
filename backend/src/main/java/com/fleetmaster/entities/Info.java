package com.fleetmaster.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "info")
public class Info {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Count is required")
    @Min(value = 0, message = "Count cannot be negative")
    @Column(nullable = false)
    private Integer count;

    // Getters / setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id < 1) {
            throw new IllegalArgumentException("The id cannot be 0 or a negative number.");
        }

        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a name.");
        }

        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        if (count == null) {
            throw new IllegalArgumentException("Please provide a count.");
        }
        
        if (count < 0) {
            throw new IllegalArgumentException("The count cannot be a negative number.");
        }

        this.count = count;
    }
}
