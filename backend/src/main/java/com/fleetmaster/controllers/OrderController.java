package com.fleetmaster.controllers;

import com.fleetmaster.dtos.AddProgressDto;
import com.fleetmaster.dtos.CreateOrderDto;
import com.fleetmaster.entities.User;
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
        User user = (User) authentication.getPrincipal();
        if (user.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("User does not belong to a company");
        }
        
        Long orderId = orderService.createOrder(user.getCompanyId(), dto);
        return ResponseEntity.ok(Map.of("message", "Order created", "orderId", orderId));
    }

    @PostMapping("/progress")
    public ResponseEntity<?> addProgress(@RequestBody AddProgressDto dto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (user.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("User does not belong to a company");
        }

        Long progressId = orderService.addProgress(user.getCompanyId(), dto);
        return ResponseEntity.ok(Map.of("message", "Progress added", "progressId", progressId));
    }
}
