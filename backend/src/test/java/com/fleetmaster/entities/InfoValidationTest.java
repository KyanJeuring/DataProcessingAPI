package com.fleetmaster.entities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InfoValidationTest {

    private Validator validator;
    private Info info;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        info = new Info();
        info.setName("System Info");
        info.setCount(100);
    }

    @Test
    void testValidInfo() {
        // Given - valid info from setUp
        
        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.isEmpty(), "Valid info should have no violations");
    }

    @Test
    void testName_Blank_Invalid() {
        // Given
        info.setName("");

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Name is required")));
    }

    @Test
    void testName_TooLong_Invalid() {
        // Given
        info.setName("a".repeat(256));

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 1 and 255 characters")));
    }

    @Test
    void testName_MaxLength_Valid() {
        // Given
        info.setName("a".repeat(255));

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_MinLength_Valid() {
        // Given
        info.setName("A");

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCount_Null_Invalid() {
        // Given
        info.setCount(null);

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Count is required")));
    }

    @Test
    void testCount_Negative_Invalid() {
        // Given
        info.setCount(-1);

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Count cannot be negative")));
    }

    @Test
    void testCount_Zero_Valid() {
        // Given
        info.setCount(0);

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCount_LargeNumber_Valid() {
        // Given
        info.setCount(999999);

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleViolations() {
        // Given - multiple invalid fields
        info.setName("");
        info.setCount(-5);

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.size() >= 2, "Should have at least 2 violations");
    }

    @Test
    void testName_WithSpaces_Valid() {
        // Given
        info.setName("System Information Item");

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_WithNumbers_Valid() {
        // Given
        info.setName("Info 2026");

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_WithSpecialCharacters_Valid() {
        // Given
        info.setName("Info-Item_123 (Active)");

        // When
        Set<ConstraintViolation<Info>> violations = validator.validate(info);

        // Then
        assertTrue(violations.isEmpty());
    }
}
