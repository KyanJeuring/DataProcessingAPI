package com.fleetmaster.entities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompanyValidationTest {

    private Validator validator;
    private Company company;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        company = new Company();
        company.setName("Tech Transport Ltd");
        company.setLicense(12345);
        company.setDiscountReceived(false);
    }

    @Test
    void testValidCompany() {
        // Given - valid company from setUp
        
        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty(), "Valid company should have no violations");
    }

    @Test
    void testName_Blank_Invalid() {
        // Given
        company.setName("");

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Company name is required")));
    }

    @Test
    void testName_TooLong_Invalid() {
        // Given
        company.setName("a".repeat(201));

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 1 and 200 characters")));
    }

    @Test
    void testName_MaxLength_Valid() {
        // Given
        company.setName("a".repeat(200));

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_MinLength_Valid() {
        // Given
        company.setName("A");

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLicense_Negative_Invalid() {
        // Given
        company.setLicense(-1);

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("License number cannot be negative")));
    }

    @Test
    void testLicense_Zero_Valid() {
        // Given
        company.setLicense(0);

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLicense_Null_Valid() {
        // Given
        company.setLicense(null);

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty(), "License is optional");
    }

    @Test
    void testLicense_LargeNumber_Valid() {
        // Given
        company.setLicense(999999999);

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDiscountReceived_True_Valid() {
        // Given
        company.setDiscountReceived(true);

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDiscountReceived_False_Valid() {
        // Given
        company.setDiscountReceived(false);

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleViolations() {
        // Given - multiple invalid fields
        company.setName("");
        company.setLicense(-5);

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertEquals(2, violations.size(), "Should have 2 violations");
    }

    @Test
    void testName_WithSpecialCharacters_Valid() {
        // Given
        company.setName("Tech & Transport Ltd. (2026)");

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_WithUnicode_Valid() {
        // Given
        company.setName("Transport Über Café");

        // When
        Set<ConstraintViolation<Company>> violations = validator.validate(company);

        // Then
        assertTrue(violations.isEmpty());
    }
}
