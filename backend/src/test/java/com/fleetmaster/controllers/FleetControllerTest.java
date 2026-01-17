package com.fleetmaster.controllers;

import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.services.FleetService;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FleetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FleetService fleetService;

    private CompanyAccount companyAccount;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        companyAccount = new CompanyAccount();
        companyAccount.setId(1L);
        companyAccount.setEmail("test@company.com");
        companyAccount.setCompanyId(100L);
        companyAccount.setVerified(true);
        
        authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(companyAccount);
    }

    @Test
    void testPing_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/fleet/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void testGetFleetStatus_Success() throws Exception {
        // Given
        Map<String, Object> fleetStatus = new HashMap<>();
        fleetStatus.put("totalVehicles", 10);
        fleetStatus.put("activeVehicles", 7);
        fleetStatus.put("inMaintenanceVehicles", 2);
        fleetStatus.put("idleVehicles", 1);

        when(fleetService.getFleetStatus(100L)).thenReturn(fleetStatus);

        // When & Then
        mockMvc.perform(get("/api/fleet/status")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVehicles").value(10))
                .andExpect(jsonPath("$.activeVehicles").value(7))
                .andExpect(jsonPath("$.inMaintenanceVehicles").value(2))
                .andExpect(jsonPath("$.idleVehicles").value(1));
    }

    @Test
    void testGetFleetStatus_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(get("/api/fleet/status")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company account does not belong to a company."));
    }

    @Test
    void testGetFleetStatus_XMLResponse() throws Exception {
        // Given
        Map<String, Object> fleetStatus = new HashMap<>();
        fleetStatus.put("totalVehicles", 10);
        fleetStatus.put("activeVehicles", 7);

        when(fleetService.getFleetStatus(100L)).thenReturn(fleetStatus);

        // When & Then
        mockMvc.perform(get("/api/fleet/status")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }

    @Test
    void testGetOrderTracking_Success() throws Exception {
        // Given
        Map<String, Object> tracking1 = new HashMap<>();
        tracking1.put("orderId", 1L);
        tracking1.put("vehicleId", 5L);
        tracking1.put("currentLocation", "Chicago");
        tracking1.put("status", "IN_TRANSIT");

        Map<String, Object> tracking2 = new HashMap<>();
        tracking2.put("orderId", 2L);
        tracking2.put("vehicleId", 6L);
        tracking2.put("currentLocation", "Denver");
        tracking2.put("status", "IN_TRANSIT");

        List<Map<String, Object>> trackingList = Arrays.asList(tracking1, tracking2);
        when(fleetService.getOrderTracking(100L)).thenReturn(trackingList);

        // When & Then
        mockMvc.perform(get("/api/fleet/tracking")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].currentLocation").value("Chicago"))
                .andExpect(jsonPath("$[1].orderId").value(2))
                .andExpect(jsonPath("$[1].currentLocation").value("Denver"));
    }

    @Test
    void testGetOrderTracking_EmptyList() throws Exception {
        // Given
        when(fleetService.getOrderTracking(100L)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/fleet/tracking")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetOrderTracking_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(get("/api/fleet/tracking")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company account does not belong to a company."));
    }

    @Test
    void testGetSubscription_Success() throws Exception {
        // Given
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("planName", "Premium");
        subscription.put("maxVehicles", 50);
        subscription.put("currentVehicles", 10);
        subscription.put("maxDrivers", 100);
        subscription.put("currentDrivers", 25);
        subscription.put("expiryDate", "2026-12-31");

        when(fleetService.getSubscriptionUsage(100L)).thenReturn(subscription);

        // When & Then
        mockMvc.perform(get("/api/fleet/subscription")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planName").value("Premium"))
                .andExpect(jsonPath("$.maxVehicles").value(50))
                .andExpect(jsonPath("$.currentVehicles").value(10))
                .andExpect(jsonPath("$.maxDrivers").value(100))
                .andExpect(jsonPath("$.currentDrivers").value(25));
    }

    @Test
    void testGetSubscription_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(get("/api/fleet/subscription")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company account does not belong to a company."));
    }

    @Test
    void testGetSubscription_CSVResponse() throws Exception {
        // Given
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("planName", "Premium");
        subscription.put("maxVehicles", 50);

        when(fleetService.getSubscriptionUsage(100L)).thenReturn(subscription);

        // When & Then
        mockMvc.perform(get("/api/fleet/subscription")
                .with(authentication(authentication))
                .accept("text/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    void testPing_NoAuthentication() throws Exception {
        // Ping endpoint should work without authentication
        // When & Then
        mockMvc.perform(get("/api/fleet/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }
}
