package com.fleetmaster.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateOrderDtoValidationTest {

    private Validator validator;
    private CreateOrderDto orderDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        orderDto = new CreateOrderDto();
        orderDto.setVehicleId(1L);
        orderDto.setDriverId(10L);
        orderDto.setPickUpLat(40.7128);
        orderDto.setPickUpLon(-74.0060);
        orderDto.setDeliveryLat(34.0522);
        orderDto.setDeliveryLon(-118.2437);
        orderDto.setLoadType("NORMAL");
        orderDto.setDepartureTime(LocalDateTime.now().plusHours(1));
        orderDto.setArrivalTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void testValidOrderDto() {
        // Given - valid DTO from setUp
        
        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
    }

    @Test
    void testVehicleId_Null_Invalid() {
        // Given
        orderDto.setVehicleId(null);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Vehicle ID is required")));
    }

    @Test
    void testDriverId_Null_Invalid() {
        // Given
        orderDto.setDriverId(null);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Driver ID is required")));
    }

    @Test
    void testPickUpLat_Null_Invalid() {
        // Given
        orderDto.setPickUpLat(null);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Pick up latitude is required")));
    }

    @Test
    void testPickUpLat_TooHigh_Invalid() {
        // Given
        orderDto.setPickUpLat(91.0);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -90 and 90")));
    }

    @Test
    void testPickUpLat_TooLow_Invalid() {
        // Given
        orderDto.setPickUpLat(-91.0);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -90 and 90")));
    }

    @Test
    void testPickUpLat_BoundaryValues_Valid() {
        // Test boundary values
        orderDto.setPickUpLat(90.0);
        Set<ConstraintViolation<CreateOrderDto>> violations1 = validator.validate(orderDto);
        assertTrue(violations1.isEmpty(), "90.0 should be valid");

        orderDto.setPickUpLat(-90.0);
        Set<ConstraintViolation<CreateOrderDto>> violations2 = validator.validate(orderDto);
        assertTrue(violations2.isEmpty(), "-90.0 should be valid");
    }

    @Test
    void testPickUpLon_TooHigh_Invalid() {
        // Given
        orderDto.setPickUpLon(181.0);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -180 and 180")));
    }

    @Test
    void testPickUpLon_TooLow_Invalid() {
        // Given
        orderDto.setPickUpLon(-181.0);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -180 and 180")));
    }

    @Test
    void testDeliveryLat_Invalid() {
        // Given
        orderDto.setDeliveryLat(100.0);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -90 and 90")));
    }

    @Test
    void testDeliveryLon_Invalid() {
        // Given
        orderDto.setDeliveryLon(200.0);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -180 and 180")));
    }

    @Test
    void testLoadType_Blank_Invalid() {
        // Given
        orderDto.setLoadType("");

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Load type is required")));
    }

    @Test
    void testLoadType_InvalidValue_Invalid() {
        // Given
        orderDto.setLoadType("INVALID_TYPE");

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("NORMAL, REFRIGERATED, or HAZARDOUS")));
    }

    @Test
    void testLoadType_AllValidValues() {
        // Test all valid load type values
        String[] validTypes = {"NORMAL", "REFRIGERATED", "HAZARDOUS"};

        for (String type : validTypes) {
            orderDto.setLoadType(type);
            Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);
            assertTrue(violations.isEmpty(), "Load type " + type + " should be valid");
        }
    }

    @Test
    void testDepartureTime_Null_Invalid() {
        // Given
        orderDto.setDepartureTime(null);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Departure time is required")));
    }

    @Test
    void testArrivalTime_Null_Invalid() {
        // Given
        orderDto.setArrivalTime(null);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Arrival time is required")));
    }

    @Test
    void testArrivalTime_Past_Invalid() {
        // Given
        orderDto.setArrivalTime(LocalDateTime.now().minusDays(1));

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must be in the future")));
    }

    @Test
    void testMultipleViolations() {
        // Given - multiple invalid fields
        orderDto.setVehicleId(null);
        orderDto.setPickUpLat(100.0);
        orderDto.setLoadType("INVALID");

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertTrue(violations.size() >= 3, "Should have at least 3 violations");
    }

    @Test
    void testCoordinates_ZeroValues_Valid() {
        // Given
        orderDto.setPickUpLat(0.0);
        orderDto.setPickUpLon(0.0);
        orderDto.setDeliveryLat(0.0);
        orderDto.setDeliveryLon(0.0);

        // When
        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(orderDto);

        // Then
        assertTrue(violations.isEmpty(), "Zero coordinates should be valid");
    }
}
