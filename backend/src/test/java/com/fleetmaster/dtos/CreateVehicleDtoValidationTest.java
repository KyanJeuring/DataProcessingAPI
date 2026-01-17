package com.fleetmaster.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateVehicleDtoValidationTest {

    private Validator validator;
    private CreateVehicleDto vehicleDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        vehicleDto = new CreateVehicleDto();
        vehicleDto.setLoadCapacity(25000L);
        vehicleDto.setType("LORRY");
        vehicleDto.setYearOfManufacture(LocalDate.of(2020, 1, 1));
        vehicleDto.setLoadTypes(Arrays.asList("NORMAL", "REFRIGERATED"));
        vehicleDto.setLastOdometer(50000L);
    }

    @Test
    void testValidVehicleDto() {
        // Given - valid DTO from setUp
        
        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
    }

    @Test
    void testLoadCapacity_Null_Invalid() {
        // Given
        vehicleDto.setLoadCapacity(null);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Load capacity is required")));
    }

    @Test
    void testLoadCapacity_Negative_Invalid() {
        // Given
        vehicleDto.setLoadCapacity(-1000L);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Load capacity cannot be negative")));
    }

    @Test
    void testLoadCapacity_Zero_Valid() {
        // Given
        vehicleDto.setLoadCapacity(0L);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testType_Blank_Invalid() {
        // Given
        vehicleDto.setType("");

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Vehicle type is required")));
    }

    @Test
    void testType_InvalidValue_Invalid() {
        // Given
        vehicleDto.setType("INVALID_TYPE");

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("LORRY, VAN, or REFRIGERATED_TRUCK")));
    }

    @Test
    void testType_AllValidValues() {
        // Test all valid type values
        String[] validTypes = {"LORRY", "VAN", "REFRIGERATED_TRUCK"};

        for (String type : validTypes) {
            vehicleDto.setType(type);
            Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);
            assertTrue(violations.isEmpty(), "Type " + type + " should be valid");
        }
    }

    @Test
    void testYearOfManufacture_Null_Invalid() {
        // Given
        vehicleDto.setYearOfManufacture(null);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Year of manufacture is required")));
    }

    @Test
    void testYearOfManufacture_Future_Invalid() {
        // Given
        vehicleDto.setYearOfManufacture(LocalDate.now().plusYears(1));

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("cannot be in the future")));
    }

    @Test
    void testYearOfManufacture_Today_Valid() {
        // Given
        vehicleDto.setYearOfManufacture(LocalDate.now());

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLoadTypes_Empty_Invalid() {
        // Given
        vehicleDto.setLoadTypes(Arrays.asList());

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Load types are required")));
    }

    @Test
    void testLoadTypes_Null_Invalid() {
        // Given
        vehicleDto.setLoadTypes(null);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Load types are required")));
    }

    @Test
    void testLoadTypes_SingleValue_Valid() {
        // Given
        vehicleDto.setLoadTypes(Arrays.asList("NORMAL"));

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLastOdometer_Null_Invalid() {
        // Given
        vehicleDto.setLastOdometer(null);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Last odometer reading is required")));
    }

    @Test
    void testLastOdometer_Negative_Invalid() {
        // Given
        vehicleDto.setLastOdometer(-1000L);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Odometer reading cannot be negative")));
    }

    @Test
    void testLastOdometer_Zero_Valid() {
        // Given
        vehicleDto.setLastOdometer(0L);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleViolations() {
        // Given - multiple invalid fields
        vehicleDto.setLoadCapacity(-100L);
        vehicleDto.setType("INVALID");
        vehicleDto.setLastOdometer(-500L);

        // When
        Set<ConstraintViolation<CreateVehicleDto>> violations = validator.validate(vehicleDto);

        // Then
        assertTrue(violations.size() >= 3, "Should have at least 3 violations");
    }
}
