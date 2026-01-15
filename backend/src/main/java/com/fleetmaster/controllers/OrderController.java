package com.fleetmaster.controllers;

import com.fleetmaster.dtos.AddProgressDto;
import com.fleetmaster.dtos.CreateOrderDto;
import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.services.OrderService;
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
@RequestMapping(value = "/api/orders", produces = {
    MediaType.APPLICATION_JSON_VALUE,
    MediaType.APPLICATION_XML_VALUE,
    "text/csv"
})
@Tag(name = "Orders", description = "Endpoints for managing transport orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create a new order", description = "Creates a new transport order for a vehicle and driver.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or company not found")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderDto dto, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }
        
        Long orderId = orderService.createOrder(companyAccount.getCompanyId(), dto);
        return ResponseEntity.ok(Map.of("message", "Order created", "orderId", orderId));
    }

    @Operation(summary = "Get all orders", description = "Retrieves all orders for the authenticated company.")
    @ApiResponse(responseCode = "200", description = "List of orders retrieved")
    @GetMapping
    public ResponseEntity<?> getAllOrders(Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        List<Map<String, Object>> orders = orderService.getAllOrders(companyAccount.getCompanyId());
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order details retrieved"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        Map<String, Object> order = orderService.getOrderById(companyAccount.getCompanyId(), id);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Update order status", description = "Updates the status of an existing order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order updated successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        orderService.updateOrderStatus(companyAccount.getCompanyId(), id, status);
        return ResponseEntity.ok(Map.of("message", "Order status updated", "orderId", id, "status", status));
    }

    @Operation(summary = "Delete order", description = "Cancels and deletes an order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        orderService.deleteOrder(companyAccount.getCompanyId(), id);
        return ResponseEntity.ok(Map.of("message", "Order deleted", "orderId", id));
    }

    @Operation(summary = "Add progress to order", description = "Adds a progress update to an existing order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progress added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or order not found")
    })
    @PostMapping("/progress")
    public ResponseEntity<?> addProgress(@RequestBody AddProgressDto dto, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        Long progressId = orderService.addProgress(companyAccount.getCompanyId(), dto);
        return ResponseEntity.ok(Map.of("message", "Progress added", "progressId", progressId));
    }

    @Operation(summary = "Get order progress", description = "Retrieves all progress updates for a specific order.")
    @ApiResponse(responseCode = "200", description = "Progress history retrieved")
    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getOrderProgress(@PathVariable Long id, Authentication authentication) {
        CompanyAccount companyAccount = (CompanyAccount) authentication.getPrincipal();
        if (companyAccount.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("CompanyAccount does not belong to a company");
        }

        List<Map<String, Object>> progress = orderService.getOrderProgress(companyAccount.getCompanyId(), id);
        return ResponseEntity.ok(progress);
    }
}
