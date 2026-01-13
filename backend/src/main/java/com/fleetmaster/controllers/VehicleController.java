package com.fleetmaster.controllers;

import com.fleetmaster.dtos.CreateVehicleDto;
import com.fleetmaster.entities.User;
import com.fleetmaster.services.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createVehicle(@RequestBody CreateVehicleDto dto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (user.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("User does not belong to a company");
        }

        Long vehicleId = vehicleService.createVehicle(user.getCompanyId(), dto);
        return ResponseEntity.ok(Map.of("message", "Vehicle created", "vehicleId", vehicleId));
    }
}
