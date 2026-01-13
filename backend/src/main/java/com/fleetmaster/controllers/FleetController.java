package com.fleetmaster.controllers;

import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.services.FleetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    private final FleetService fleetService;

    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/status")
    public ResponseEntity<?> getFleetStatus(Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("Company account does not belong to a company.");
        }
        return ResponseEntity.ok(fleetService.getFleetStatus(companyAccount.getCompanyId()));
    }

    @GetMapping("/tracking")
    public ResponseEntity<?> getOrderTracking(Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("Company account does not belong to a company.");
        }
        return ResponseEntity.ok(fleetService.getOrderTracking(companyAccount.getCompanyId()));
    }

    @GetMapping("/subscription")
    public ResponseEntity<?> getSubscription(Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("Company account does not belong to a company.");
        }
        return ResponseEntity.ok(fleetService.getSubscriptionUsage(companyAccount.getCompanyId()));
    }
}
