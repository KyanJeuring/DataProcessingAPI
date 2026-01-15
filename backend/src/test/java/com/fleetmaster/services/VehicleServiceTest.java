package com.fleetmaster.services;

import com.fleetmaster.dtos.CreateVehicleDto;
import com.fleetmaster.exceptions.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Mock
    private NativeQuery nativeQuery;

    @InjectMocks
    private VehicleService vehicleService;

    private CreateVehicleDto createVehicleDto;

    @BeforeEach
    void setUp() {
        createVehicleDto = new CreateVehicleDto();
        createVehicleDto.setType("TRUCK");
        createVehicleDto.setLoadCapacity(5000L);
        createVehicleDto.setYearOfManufacture(LocalDate.of(2020, 1, 1));
        createVehicleDto.setLoadTypes(Arrays.asList("NORMAL", "HAZARDOUS"));
        createVehicleDto.setLastOdometer(100000L);
    }

    @Test
    void testCreateVehicle_Success() {
        // Given
        Long companyId = 1L;
        Long expectedVehicleId = 10L;

        Query insertVehicleQuery = mock(Query.class);
        Query linkCompanyQuery = mock(Query.class);

        when(entityManager.createNativeQuery(contains("INSERT INTO vehicles")))
                .thenReturn(insertVehicleQuery);
        when(insertVehicleQuery.setParameter(anyString(), any())).thenReturn(insertVehicleQuery);
        when(insertVehicleQuery.getSingleResult()).thenReturn(expectedVehicleId);

        when(entityManager.createNativeQuery(contains("INSERT INTO company_vehicles")))
                .thenReturn(linkCompanyQuery);
        when(linkCompanyQuery.setParameter(anyString(), any())).thenReturn(linkCompanyQuery);
        when(linkCompanyQuery.executeUpdate()).thenReturn(1);

        // When
        Long vehicleId = vehicleService.createVehicle(companyId, createVehicleDto);

        // Then
        assertNotNull(vehicleId);
        assertEquals(expectedVehicleId, vehicleId);
        verify(entityManager, times(2)).createNativeQuery(anyString());
        verify(insertVehicleQuery).getSingleResult();
        verify(linkCompanyQuery).executeUpdate();
    }

    @Test
    void testGetAllVehicles_Success() {
        // Given
        Long companyId = 1L;
        List<Map<String, Object>> expectedVehicles = new ArrayList<>();
        Map<String, Object> vehicle1 = new HashMap<>();
        vehicle1.put("id", 1L);
        vehicle1.put("type", "TRUCK");
        vehicle1.put("load_capacity", 5000L);
        expectedVehicles.add(vehicle1);

        when(entityManager.createNativeQuery(contains("SELECT v.* FROM vehicles")))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(expectedVehicles);

        // When
        List<Map<String, Object>> vehicles = vehicleService.getAllVehicles(companyId);

        // Then
        assertNotNull(vehicles);
        assertEquals(1, vehicles.size());
        assertEquals(1L, vehicles.get(0).get("id"));
        verify(entityManager).createNativeQuery(contains("SELECT v.* FROM vehicles"));
    }

    @Test
    void testGetVehicleById_Success() {
        // Given
        Long companyId = 1L;
        Long vehicleId = 10L;
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("id", vehicleId);
        vehicle.put("type", "TRUCK");
        results.add(vehicle);

        when(entityManager.createNativeQuery(contains("SELECT v.* FROM vehicles")))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(results);

        // When
        Map<String, Object> result = vehicleService.getVehicleById(companyId, vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(vehicleId, result.get("id"));
        verify(entityManager).createNativeQuery(contains("SELECT v.* FROM vehicles"));
    }

    @Test
    void testGetVehicleById_NotFound() {
        // Given
        Long companyId = 1L;
        Long vehicleId = 999L;

        when(entityManager.createNativeQuery(contains("SELECT v.* FROM vehicles")))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            vehicleService.getVehicleById(companyId, vehicleId);
        });

        assertEquals("Vehicle not found or does not belong to your company", exception.getMessage());
    }

    @Test
    void testUpdateVehicle_Success() {
        // Given
        Long companyId = 1L;
        Long vehicleId = 10L;

        // Mock getVehicleById (ownership check)
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("id", vehicleId);
        results.add(vehicle);

        Query selectQuery = mock(Query.class);
        Query updateQuery = mock(Query.class);
        NativeQuery selectNativeQuery = mock(NativeQuery.class);

        when(entityManager.createNativeQuery(contains("SELECT v.* FROM vehicles")))
                .thenReturn(selectQuery);
        when(selectQuery.setParameter(anyString(), any())).thenReturn(selectQuery);
        when(selectQuery.unwrap(NativeQuery.class)).thenReturn(selectNativeQuery);
        when(selectNativeQuery.setResultTransformer(any())).thenReturn(selectNativeQuery);
        when(selectNativeQuery.getResultList()).thenReturn(results);

        when(entityManager.createNativeQuery(contains("UPDATE vehicles SET")))
                .thenReturn(updateQuery);
        when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1);

        // When
        assertDoesNotThrow(() -> vehicleService.updateVehicle(companyId, vehicleId, createVehicleDto));

        // Then
        verify(updateQuery).executeUpdate();
    }

    @Test
    void testUpdateVehicle_Unauthorized() {
        // Given
        Long companyId = 1L;
        Long vehicleId = 999L;

        when(entityManager.createNativeQuery(contains("SELECT v.* FROM vehicles")))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            vehicleService.updateVehicle(companyId, vehicleId, createVehicleDto);
        });

        assertEquals("Vehicle not found or does not belong to your company", exception.getMessage());
    }

    @Test
    void testDeleteVehicle_Success() {
        // Given
        Long companyId = 1L;
        Long vehicleId = 10L;

        // Mock getVehicleById (ownership check)
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("id", vehicleId);
        results.add(vehicle);

        Query selectQuery = mock(Query.class);
        Query deleteCompanyVehiclesQuery = mock(Query.class);
        Query deleteVehicleQuery = mock(Query.class);
        NativeQuery selectNativeQuery = mock(NativeQuery.class);

        when(entityManager.createNativeQuery(contains("SELECT v.* FROM vehicles")))
                .thenReturn(selectQuery);
        when(selectQuery.setParameter(anyString(), any())).thenReturn(selectQuery);
        when(selectQuery.unwrap(NativeQuery.class)).thenReturn(selectNativeQuery);
        when(selectNativeQuery.setResultTransformer(any())).thenReturn(selectNativeQuery);
        when(selectNativeQuery.getResultList()).thenReturn(results);

        when(entityManager.createNativeQuery(contains("DELETE FROM company_vehicles")))
                .thenReturn(deleteCompanyVehiclesQuery);
        when(deleteCompanyVehiclesQuery.setParameter(anyString(), any())).thenReturn(deleteCompanyVehiclesQuery);
        when(deleteCompanyVehiclesQuery.executeUpdate()).thenReturn(1);

        when(entityManager.createNativeQuery(contains("DELETE FROM vehicles")))
                .thenReturn(deleteVehicleQuery);
        when(deleteVehicleQuery.setParameter(anyString(), any())).thenReturn(deleteVehicleQuery);
        when(deleteVehicleQuery.executeUpdate()).thenReturn(1);

        // When
        assertDoesNotThrow(() -> vehicleService.deleteVehicle(companyId, vehicleId));

        // Then
        verify(deleteCompanyVehiclesQuery).executeUpdate();
        verify(deleteVehicleQuery).executeUpdate();
    }
}
