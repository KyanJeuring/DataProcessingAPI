package com.fleetmaster.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fleetmaster.entities.Company;
import com.fleetmaster.repositories.CompanyRepository;

@RestController
@RequestMapping("/api/companies") //http://localhost:8080/api/companies
public class CompanyController {

  private final CompanyRepository repository;

  public CompanyController(CompanyRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<Company> list() {
    return repository.findAll(); // GET http://localhost:8080/api/companies
  }

  @PostMapping
  public ResponseEntity<Company> create(@RequestBody Company incoming) {
    if (incoming.getName() == null || incoming.getName().trim().isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    Company company = new Company();
    company.setName(incoming.getName().trim());
    company.setLicense(incoming.getLicense());
    company.setDiscountReceived(incoming.isDiscountReceived());
    Company saved = repository.save(company);
    return new ResponseEntity<>(saved, HttpStatus.CREATED);
  }
}
