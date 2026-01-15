package com.fleetmaster.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fleetmaster.entities.Company;
import com.fleetmaster.repositories.CompanyRepository;

@RestController
@RequestMapping(value = "/api/companies", produces = {
    MediaType.APPLICATION_JSON_VALUE,
    MediaType.APPLICATION_XML_VALUE,
    "text/csv"
})
@Tag(name = "Companies", description = "Endpoints for managing companies")
public class CompanyController {

  private final CompanyRepository repository;

  public CompanyController(CompanyRepository repository) {
    this.repository = repository;
  }

  @Operation(summary = "Get all companies", description = "Retrieves a list of all registered companies.")
  @ApiResponse(responseCode = "200", description = "List of companies retrieved")
  @GetMapping
  public List<Company> list() {
    return repository.findAll();
  }

  @Operation(summary = "Create a new company", description = "Creates a new company in the system.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Company created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
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
