package com.fleetmaster.controllers;

import com.fleetmaster.entities.*;
import com.fleetmaster.services.InfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/database/info")
public class InfoController {

    private final InfoService infoService;

    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

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
