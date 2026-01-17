package com.fleetmaster.entities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApiAccountValidationTest {

    private Validator validator;
    private ApiAccount apiAccount;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        apiAccount = new ApiAccount();
        apiAccount.setUsername("apiuser");
        apiAccount.setPasswordHash("hashedpassword123");
        apiAccount.setActive(true);
    }

    @Test
    void testValidApiAccount() {
        // Given - valid account from setUp
        
        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertTrue(violations.isEmpty(), "Valid API account should have no violations");
    }

    @Test
    void testUsername_Blank_Invalid() {
        // Given
        apiAccount.setUsername("");

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username is required")));
    }

    @Test
    void testUsername_TooShort_Invalid() {
        // Given
        apiAccount.setUsername("ab");

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 50 characters")));
    }

    @Test
    void testUsername_TooLong_Invalid() {
        // Given
        apiAccount.setUsername("a".repeat(51));

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 50 characters")));
    }

    @Test
    void testUsername_MinLength_Valid() {
        // Given
        apiAccount.setUsername("abc");

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUsername_MaxLength_Valid() {
        // Given
        apiAccount.setUsername("a".repeat(50));

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPasswordHash_Blank_Invalid() {
        // Given
        apiAccount.setPasswordHash("");

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password hash is required")));
    }

    @Test
    void testPasswordHash_Null_Invalid() {
        // Given
        apiAccount.setPasswordHash(null);

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password hash is required")));
    }

    @Test
    void testActive_True_Valid() {
        // Given
        apiAccount.setActive(true);

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testActive_False_Valid() {
        // Given
        apiAccount.setActive(false);

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleViolations() {
        // Given - multiple invalid fields
        apiAccount.setUsername("ab");
        apiAccount.setPasswordHash("");

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertTrue(violations.size() >= 2, "Should have at least 2 violations");
    }

    @Test
    void testUsername_WithNumbers_Valid() {
        // Given
        apiAccount.setUsername("api123user");

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUsername_WithUnderscores_Valid() {
        // Given
        apiAccount.setUsername("api_user_123");

        // When
        Set<ConstraintViolation<ApiAccount>> violations = validator.validate(apiAccount);

        // Then
        assertTrue(violations.isEmpty());
    }
}
