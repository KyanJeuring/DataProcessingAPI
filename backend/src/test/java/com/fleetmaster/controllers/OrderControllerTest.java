package com.fleetmaster.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetmaster.dtos.AddProgressDto;
import com.fleetmaster.dtos.CreateOrderDto;
import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private CompanyAccount companyAccount;
    private CreateOrderDto orderDto;
    private AddProgressDto progressDto;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        companyAccount = new CompanyAccount();
        companyAccount.setId(1L);
        companyAccount.setEmail("test@company.com");
        companyAccount.setCompanyId(100L);
        companyAccount.setVerified(true);

        orderDto = new CreateOrderDto();
        orderDto.setVehicleId(1L);
        orderDto.setDriverId(10L);
        orderDto.setOrigin("New York");
        orderDto.setDestination("Los Angeles");
        orderDto.setCargoWeight(15000.0);

        progressDto = new AddProgressDto();
        progressDto.setOrderId(1L);
        progressDto.setLocation("Chicago");
        progressDto.setStatus("IN_TRANSIT");
        progressDto.setNotes("Halfway through the journey");
        
        authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(companyAccount);
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        // Given
        when(orderService.createOrder(eq(100L), any(CreateOrderDto.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/orders/create")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order created"))
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void testCreateOrder_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(post("/api/orders/create")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testGetAllOrders_Success() throws Exception {
        // Given
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", 1L);
        order1.put("origin", "New York");
        order1.put("destination", "Los Angeles");
        order1.put("status", "PENDING");

        Map<String, Object> order2 = new HashMap<>();
        order2.put("id", 2L);
        order2.put("origin", "Chicago");
        order2.put("destination", "Miami");
        order2.put("status", "COMPLETED");

        List<Map<String, Object>> orders = Arrays.asList(order1, order2);
        when(orderService.getAllOrders(100L)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].origin").value("New York"))
                .andExpect(jsonPath("$[1].origin").value("Chicago"));
    }

    @Test
    void testGetAllOrders_EmptyList() throws Exception {
        // Given
        when(orderService.getAllOrders(100L)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/orders")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetAllOrders_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(get("/api/orders")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testGetOrderById_Success() throws Exception {
        // Given
        Map<String, Object> order = new HashMap<>();
        order.put("id", 1L);
        order.put("origin", "New York");
        order.put("destination", "Los Angeles");
        order.put("status", "IN_TRANSIT");
        order.put("cargoWeight", 15000.0);

        when(orderService.getOrderById(100L, 1L)).thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/orders/1")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.origin").value("New York"))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    void testGetOrderById_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(get("/api/orders/1")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testUpdateOrderStatus_Success() throws Exception {
        // Given
        doNothing().when(orderService).updateOrderStatus(100L, 1L, "COMPLETED");

        // When & Then
        mockMvc.perform(put("/api/orders/1/status")
                .with(authentication(authentication))
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order status updated"))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void testUpdateOrderStatus_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(put("/api/orders/1/status")
                .with(authentication(authentication))
                .param("status", "COMPLETED"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testDeleteOrder_Success() throws Exception {
        // Given
        doNothing().when(orderService).deleteOrder(100L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/orders/1")
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order deleted"))
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void testDeleteOrder_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(delete("/api/orders/1")
                .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testAddProgress_Success() throws Exception {
        // Given
        when(orderService.addProgress(eq(100L), any(AddProgressDto.class))).thenReturn(10L);

        // When & Then
        mockMvc.perform(post("/api/orders/progress")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(progressDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Progress added"))
                .andExpect(jsonPath("$.progressId").value(10));
    }

    @Test
    void testAddProgress_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(post("/api/orders/progress")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(progressDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testGetOrderProgress_Success() throws Exception {
        // Given
        Map<String, Object> progress1 = new HashMap<>();
        progress1.put("id", 10L);
        progress1.put("location", "Chicago");
        progress1.put("status", "IN_TRANSIT");

        Map<String, Object> progress2 = new HashMap<>();
        progress2.put("id", 11L);
        progress2.put("location", "Denver");
        progress2.put("status", "IN_TRANSIT");

        List<Map<String, Object>> progressList = Arrays.asList(progress1, progress2);
        when(orderService.getOrderProgress(100L, 1L)).thenReturn(progressList);

        // When & Then
        mockMvc.perform(get("/api/orders/1/progress")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].location").value("Chicago"))
                .andExpect(jsonPath("$[1].location").value("Denver"));
    }

    @Test
    void testGetOrderProgress_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(get("/api/orders/1/progress")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testCreateOrder_XMLResponse() throws Exception {
        // Given
        when(orderService.createOrder(eq(100L), any(CreateOrderDto.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/orders/create")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }
}
