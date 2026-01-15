package com.fleetmaster.controllers;

import com.fleetmaster.entities.*;
import com.fleetmaster.services.InfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/database/info")
@Tag(name = "Info", description = "Endpoints for retrieving general information")
public class InfoController {

    private final InfoService infoService;

    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    @Operation(summary = "Get all info", description = "Retrieves a list of all info entries. Requires authenticated and verified company account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of info retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Company account not verified or blocked")
    })
    @GetMapping("/get")
    public ResponseEntity<?> getAllInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (!companyAccount.isVerified()) {
            return ResponseEntity.status(403).body("Company account not verified");
        }
        if ("BLOCKED".equals(companyAccount.getAccountStatus())) {
            return ResponseEntity.status(403).body("Company account blocked");
        }

        List<Info> infos = infoService.getAll();
        return ResponseEntity.ok(infos);
    }
}
