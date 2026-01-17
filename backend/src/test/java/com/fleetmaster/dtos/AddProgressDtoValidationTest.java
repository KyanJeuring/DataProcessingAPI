package com.fleetmaster.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AddProgressDtoValidationTest {

    private Validator validator;
    private AddProgressDto progressDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        progressDto = new AddProgressDto();
        progressDto.setOrderId(1L);
        progressDto.setLat(40.7128);
        progressDto.setLon(-74.0060);
        progressDto.setType("IN_TRANSIT");
    }

    @Test
    void testValidProgressDto() {
        // Given - valid DTO from setUp
        
        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
    }

    @Test
    void testOrderId_Null_Invalid() {
        // Given
        progressDto.setOrderId(null);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Order ID is required")));
    }

    @Test
    void testLat_Null_Invalid() {
        // Given
        progressDto.setLat(null);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Latitude is required")));
    }

    @Test
    void testLat_TooHigh_Invalid() {
        // Given
        progressDto.setLat(91.0);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -90 and 90")));
    }

    @Test
    void testLat_TooLow_Invalid() {
        // Given
        progressDto.setLat(-91.0);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -90 and 90")));
    }

    @Test
    void testLat_BoundaryValues_Valid() {
        // Test boundary values
        progressDto.setLat(90.0);
        Set<ConstraintViolation<AddProgressDto>> violations1 = validator.validate(progressDto);
        assertTrue(violations1.isEmpty(), "90.0 should be valid");

        progressDto.setLat(-90.0);
        Set<ConstraintViolation<AddProgressDto>> violations2 = validator.validate(progressDto);
        assertTrue(violations2.isEmpty(), "-90.0 should be valid");
    }

    @Test
    void testLon_Null_Invalid() {
        // Given
        progressDto.setLon(null);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Longitude is required")));
    }

    @Test
    void testLon_TooHigh_Invalid() {
        // Given
        progressDto.setLon(181.0);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -180 and 180")));
    }

    @Test
    void testLon_TooLow_Invalid() {
        // Given
        progressDto.setLon(-181.0);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between -180 and 180")));
    }

    @Test
    void testLon_BoundaryValues_Valid() {
        // Test boundary values
        progressDto.setLon(180.0);
        Set<ConstraintViolation<AddProgressDto>> violations1 = validator.validate(progressDto);
        assertTrue(violations1.isEmpty(), "180.0 should be valid");

        progressDto.setLon(-180.0);
        Set<ConstraintViolation<AddProgressDto>> violations2 = validator.validate(progressDto);
        assertTrue(violations2.isEmpty(), "-180.0 should be valid");
    }

    @Test
    void testType_Blank_Invalid() {
        // Given
        progressDto.setType("");

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Progress type is required")));
    }

    @Test
    void testType_Null_Invalid() {
        // Given
        progressDto.setType(null);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Progress type is required")));
    }

    @Test
    void testDescription_Null_Valid() {
        // Given
        progressDto.setDescription(null);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertTrue(violations.isEmpty(), "Description is optional");
    }

    @Test
    void testDescription_WithData_Valid() {
        // Given
        Map<String, Object> description = new HashMap<>();
        description.put("note", "Stopped for fuel");
        description.put("duration", 15);
        progressDto.setDescription(description);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleViolations() {
        // Given - multiple invalid fields
        progressDto.setOrderId(null);
        progressDto.setLat(100.0);
        progressDto.setLon(200.0);
        progressDto.setType("");

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertTrue(violations.size() >= 4, "Should have at least 4 violations");
    }

    @Test
    void testCoordinates_ZeroValues_Valid() {
        // Given
        progressDto.setLat(0.0);
        progressDto.setLon(0.0);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertTrue(violations.isEmpty(), "Zero coordinates should be valid");
    }

    @Test
    void testType_DifferentValues_Valid() {
        // Test various type values
        String[] types = {"IN_TRANSIT", "ARRIVED", "DELAYED", "REFUELING"};

        for (String type : types) {
            progressDto.setType(type);
            Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);
            assertTrue(violations.isEmpty(), "Type " + type + " should be valid");
        }
    }

    @Test
    void testCoordinates_HighPrecision_Valid() {
        // Given
        progressDto.setLat(40.712776);
        progressDto.setLon(-74.005974);

        // When
        Set<ConstraintViolation<AddProgressDto>> violations = validator.validate(progressDto);

        // Then
        assertTrue(violations.isEmpty());
    }
}
