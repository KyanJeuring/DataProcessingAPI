package com.fleetmaster.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "company")
public class Company {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column
  private Integer license;

  @Column(nullable = false)
  private boolean discountReceived;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
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
