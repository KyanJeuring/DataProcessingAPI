package com.fleetmaster.controllers;

import com.fleetmaster.dtos.CreateVehicleDto;
import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.services.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/vehicles", produces = {
    MediaType.APPLICATION_JSON_VALUE,
    MediaType.APPLICATION_XML_VALUE,
    "text/csv"
})
@Tag(name = "Vehicles", description = "Endpoints for managing fleet vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Operation(summary = "Create a new vehicle", description = "Creates a new vehicle and assigns it to the authenticated company.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehicle created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or company not found")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createVehicle(@RequestBody CreateVehicleDto dto, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        Long vehicleId = vehicleService.createVehicle(companyAccount.getCompanyId(), dto);
        return ResponseEntity.ok(Map.of("message", "Vehicle created", "vehicleId", vehicleId));
    }

    @Operation(summary = "Get all vehicles", description = "Retrieves all vehicles for the authenticated company.")
    @ApiResponse(responseCode = "200", description = "List of vehicles retrieved")
    @GetMapping
    public ResponseEntity<?> getAllVehicles(Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        List<Map<String, Object>> vehicles = vehicleService.getAllVehicles(companyAccount.getCompanyId());
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Get vehicle by ID", description = "Retrieves a specific vehicle by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehicle details retrieved"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        Map<String, Object> vehicle = vehicleService.getVehicleById(companyAccount.getCompanyId(), id);
        return ResponseEntity.ok(vehicle);
    }

    @Operation(summary = "Update vehicle", description = "Updates an existing vehicle's information.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody CreateVehicleDto dto, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        vehicleService.updateVehicle(companyAccount.getCompanyId(), id, dto);
        return ResponseEntity.ok(Map.of("message", "Vehicle updated", "vehicleId", id));
    }

    @Operation(summary = "Delete vehicle", description = "Deletes a vehicle from the fleet.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehicle deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        vehicleService.deleteVehicle(companyAccount.getCompanyId(), id);
        return ResponseEntity.ok(Map.of("message", "Vehicle deleted", "vehicleId", id));
    }
}
