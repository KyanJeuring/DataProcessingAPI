package com.fleetmaster.controllers;

import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.services.FleetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/fleet", produces = {
    MediaType.APPLICATION_JSON_VALUE,
    MediaType.APPLICATION_XML_VALUE,
    "text/csv"
})
@Tag(name = "Fleet", description = "Endpoints for fleet management and monitoring")
public class FleetController {

    private final FleetService fleetService;

    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @Operation(summary = "Ping endpoint", description = "Simple health check endpoint.")
    @ApiResponse(responseCode = "200", description = "Service is running")
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("pong");
    }

    @Operation(summary = "Get fleet status", description = "Retrieves the current status of all vehicles in the company's fleet.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fleet status retrieved"),
        @ApiResponse(responseCode = "400", description = "Company account not valid")
    })
    @GetMapping("/status")
    public ResponseEntity<?> getFleetStatus(Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("Company account does not belong to a company.");
        }
        return ResponseEntity.ok(fleetService.getFleetStatus(companyAccount.getCompanyId()));
    }

    @Operation(summary = "Get order tracking", description = "Retrieves real-time tracking information for all active orders.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order tracking data retrieved"),
        @ApiResponse(responseCode = "400", description = "Company account not valid")
    })
    @GetMapping("/tracking")
    public ResponseEntity<?> getOrderTracking(Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("Company account does not belong to a company.");
        }
        return ResponseEntity.ok(fleetService.getOrderTracking(companyAccount.getCompanyId()));
    }

    @Operation(summary = "Get subscription usage", description = "Retrieves the company's subscription details and resource usage.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription data retrieved"),
        @ApiResponse(responseCode = "400", description = "Company account not valid")
    })
    @GetMapping("/subscription")
    public ResponseEntity<?> getSubscription(Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("Company account does not belong to a company.");
        }
        return ResponseEntity.ok(fleetService.getSubscriptionUsage(companyAccount.getCompanyId()));
    }
}
