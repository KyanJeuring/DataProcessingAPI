package com.fleetmaster.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetmaster.dtos.CreateVehicleDto;
import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.services.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
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
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    private CompanyAccount companyAccount;
    private CreateVehicleDto vehicleDto;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        companyAccount = new CompanyAccount();
        companyAccount.setId(1L);
        companyAccount.setEmail("test@company.com");
        companyAccount.setCompanyId(100L);
        companyAccount.setVerified(true);

        vehicleDto = new CreateVehicleDto();
        vehicleDto.setLicensePlate("ABC-123");
        vehicleDto.setBrand("Volvo");
        vehicleDto.setModel("FH16");
        vehicleDto.setCapacity(25000.0);
        
        authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(companyAccount);
    }

    @Test
    void testCreateVehicle_Success() throws Exception {
        // Given
        when(vehicleService.createVehicle(eq(100L), any(CreateVehicleDto.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/vehicles/create")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle created"))
                .andExpect(jsonPath("$.vehicleId").value(1));
    }

    @Test
    void testCreateVehicle_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(post("/api/vehicles/create")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testGetAllVehicles_Success() throws Exception {
        // Given
        Map<String, Object> vehicle1 = new HashMap<>();
        vehicle1.put("id", 1L);
        vehicle1.put("licensePlate", "ABC-123");
        vehicle1.put("brand", "Volvo");

        Map<String, Object> vehicle2 = new HashMap<>();
        vehicle2.put("id", 2L);
        vehicle2.put("licensePlate", "XYZ-456");
        vehicle2.put("brand", "Scania");

        List<Map<String, Object>> vehicles = Arrays.asList(vehicle1, vehicle2);
        when(vehicleService.getAllVehicles(100L)).thenReturn(vehicles);

        // When & Then
        mockMvc.perform(get("/api/vehicles")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$[1].licensePlate").value("XYZ-456"));
    }

    @Test
    void testGetAllVehicles_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(get("/api/vehicles")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testGetVehicleById_Success() throws Exception {
        // Given
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("id", 1L);
        vehicle.put("licensePlate", "ABC-123");
        vehicle.put("brand", "Volvo");
        vehicle.put("model", "FH16");
        vehicle.put("capacity", 25000.0);

        when(vehicleService.getVehicleById(100L, 1L)).thenReturn(vehicle);

        // When & Then
        mockMvc.perform(get("/api/vehicles/1")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$.brand").value("Volvo"));
    }

    @Test
    void testGetVehicleById_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(get("/api/vehicles/1")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testUpdateVehicle_Success() throws Exception {
        // Given
        doNothing().when(vehicleService).updateVehicle(eq(100L), eq(1L), any(CreateVehicleDto.class));

        // When & Then
        mockMvc.perform(put("/api/vehicles/1")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle updated"))
                .andExpect(jsonPath("$.vehicleId").value(1));
    }

    @Test
    void testUpdateVehicle_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(put("/api/vehicles/1")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testDeleteVehicle_Success() throws Exception {
        // Given
        doNothing().when(vehicleService).deleteVehicle(100L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/vehicles/1")
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle deleted"))
                .andExpect(jsonPath("$.vehicleId").value(1));
    }

    @Test
    void testDeleteVehicle_NoCompanyId_BadRequest() throws Exception {
        // Given
        companyAccount.setCompanyId(null);

        // When & Then
        mockMvc.perform(delete("/api/vehicles/1")
                .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CompanyAccount does not belong to a company"));
    }

    @Test
    void testCreateVehicle_XMLResponse() throws Exception {
        // Given
        when(vehicleService.createVehicle(eq(100L), any(CreateVehicleDto.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/vehicles/create")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }

    @Test
    void testGetAllVehicles_EmptyList() throws Exception {
        // Given
        when(vehicleService.getAllVehicles(100L)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/vehicles")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
