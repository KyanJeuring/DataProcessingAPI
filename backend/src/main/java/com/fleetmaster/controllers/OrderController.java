package com.fleetmaster.controllers;

import com.fleetmaster.dtos.AddProgressDto;
import com.fleetmaster.dtos.CreateOrderDto;
import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderDto dto, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }
        
        Long orderId = orderService.createOrder(companyAccount.getCompanyId(), dto);
        return ResponseEntity.ok(Map.of("message", "Order created", "orderId", orderId));
    }

    @PostMapping("/progress")
    public ResponseEntity<?> addProgress(@RequestBody AddProgressDto dto, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        Long progressId = orderService.addProgress(companyAccount.getCompanyId(), dto);
        return ResponseEntity.ok(Map.of("message", "Progress added", "progressId", progressId));
    }
}
