package com.fleetmaster.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "company")
public class Company {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Company name is required")
  @Size(min = 1, max = 200, message = "Company name must be between 1 and 200 characters")
  @Column(nullable = false)
  private String name;

  @Min(value = 0, message = "License number cannot be negative")
  @Column
  private Integer license;

  @Column(nullable = false)
  private boolean discountReceived;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    if (id < 0) {
      throw new IllegalArgumentException("The ID cannot be a negative number.");
    }

    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Please provide a company name.");
    }

    this.name = name;
  }

  public Integer getLicense() {
    return license;
  }

  public void setLicense(Integer license) {
    this.license = license;
  }

  public boolean isDiscountReceived() {
    return discountReceived;
  }

  public void setDiscountReceived(boolean discountReceived) {
    this.discountReceived = discountReceived;
  }
}
