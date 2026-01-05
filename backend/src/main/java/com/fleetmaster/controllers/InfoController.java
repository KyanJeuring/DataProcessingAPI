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

        User user = (User) authentication.getPrincipal();
        if (!user.isVerified()) {
            return ResponseEntity.status(403).body("User not verified");
        }
        if ("BLOCKED".equals(user.getStatus())) {
            return ResponseEntity.status(403).body("User blocked");
        }

        List<Info> infos = infoService.getAll();
        return ResponseEntity.ok(infos);
    }
}
