package com.fleetmaster.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginDtoValidationTest {

    private Validator validator;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");
    }

    @Test
    void testValidLoginDto() {
        // Given - valid DTO from setUp
        
        // When
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
    }

    @Test
    void testEmail_Blank_Invalid() {
        // Given
        loginDto.setEmail("");

        // When
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void testEmail_Null_Invalid() {
        // Given
        loginDto.setEmail(null);

        // When
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void testEmail_InvalidFormat_Invalid() {
        // Given
        loginDto.setEmail("notanemail");

        // When
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }

    @Test
    void testEmail_MissingAtSign_Invalid() {
        // Given
        loginDto.setEmail("testexample.com");

        // When
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void testPassword_Blank_Invalid() {
        // Given
        loginDto.setPassword("");

        // When
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void testPassword_Null_Invalid() {
        // Given
        loginDto.setPassword(null);

        // When
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void testMultipleViolations() {
        // Given - both fields invalid
        loginDto.setEmail("invalid");
        loginDto.setPassword("");

        // When
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        // Then
        assertTrue(violations.size() >= 2, "Should have at least 2 violations");
    }

    @Test
    void testValidEmailFormats() {
        // Test various valid email formats
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com"
        };

        for (String email : validEmails) {
            loginDto.setEmail(email);
            Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);
            assertTrue(violations.isEmpty(), "Email " + email + " should be valid");
        }
    }
}
