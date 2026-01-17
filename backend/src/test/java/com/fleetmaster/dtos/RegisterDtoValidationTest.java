package com.fleetmaster.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterDtoValidationTest {

    private Validator validator;
    private RegisterDto registerDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");
        registerDto.setCompanyName("Test Company");
    }

    @Test
    void testValidRegisterDto() {
        // Given - valid DTO from setUp
        
        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
    }

    @Test
    void testUsername_Blank_Invalid() {
        // Given
        registerDto.setUsername("");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username is required")));
    }

    @Test
    void testUsername_TooShort_Invalid() {
        // Given
        registerDto.setUsername("ab");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 50 characters")));
    }

    @Test
    void testUsername_TooLong_Invalid() {
        // Given
        registerDto.setUsername("a".repeat(51));

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void testEmail_Blank_Invalid() {
        // Given
        registerDto.setEmail("");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void testEmail_InvalidFormat_Invalid() {
        // Given
        registerDto.setEmail("notanemail");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }

    @Test
    void testPassword_Blank_Invalid() {
        // Given
        registerDto.setPassword("");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void testPassword_TooShort_Invalid() {
        // Given
        registerDto.setPassword("pass123");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("at least 8 characters")));
    }

    @Test
    void testPassword_MinLength_Valid() {
        // Given
        registerDto.setPassword("12345678");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFirstName_TooLong_Invalid() {
        // Given
        registerDto.setFirstName("a".repeat(101));

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("First name must not exceed 100 characters")));
    }

    @Test
    void testFirstName_Null_Valid() {
        // Given
        registerDto.setFirstName(null);

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertTrue(violations.isEmpty(), "First name is optional");
    }

    @Test
    void testLastName_TooLong_Invalid() {
        // Given
        registerDto.setLastName("a".repeat(101));

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Last name must not exceed 100 characters")));
    }

    @Test
    void testCompanyName_Blank_Invalid() {
        // Given
        registerDto.setCompanyName("");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Company name is required")));
    }

    @Test
    void testCompanyName_TooLong_Invalid() {
        // Given
        registerDto.setCompanyName("a".repeat(201));

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 1 and 200 characters")));
    }

    @Test
    void testMultipleViolations() {
        // Given - multiple invalid fields
        registerDto.setUsername("ab");
        registerDto.setEmail("invalid");
        registerDto.setPassword("short");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertTrue(violations.size() >= 3, "Should have at least 3 violations");
    }

    @Test
    void testAllFieldsValid() {
        // Given
        registerDto.setUsername("validuser");
        registerDto.setEmail("valid@example.com");
        registerDto.setPassword("SecurePassword123");
        registerDto.setFirstName("John");
        registerDto.setLastName("Doe");
        registerDto.setCompanyName("Valid Company Ltd");

        // When
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        // Then
        assertTrue(violations.isEmpty());
    }
}
