package com.fleetmaster.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fleetmaster.entities.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
