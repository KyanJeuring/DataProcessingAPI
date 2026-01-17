package com.fleetmaster.entities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompanyAccountValidationTest {

    private Validator validator;
    private CompanyAccount companyAccount;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        companyAccount = new CompanyAccount();
        companyAccount.setUsername("testuser");
        companyAccount.setEmail("test@example.com");
        companyAccount.setPasswordHash("hashedpassword123");
    }

    @Test
    void testValidCompanyAccount() {
        // Given - valid account from setUp
        
        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertTrue(violations.isEmpty(), "Valid account should have no violations");
    }

    @Test
    void testUsername_Blank_Invalid() {
        // Given
        companyAccount.setUsername("");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username is required")));
    }

    @Test
    void testUsername_TooShort_Invalid() {
        // Given
        companyAccount.setUsername("ab");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 50 characters")));
    }

    @Test
    void testUsername_TooLong_Invalid() {
        // Given
        companyAccount.setUsername("a".repeat(51));

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 50 characters")));
    }

    @Test
    void testUsername_MinLength_Valid() {
        // Given
        companyAccount.setUsername("abc");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUsername_MaxLength_Valid() {
        // Given
        companyAccount.setUsername("a".repeat(50));

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmail_Blank_Invalid() {
        // Given
        companyAccount.setEmail("");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void testEmail_InvalidFormat_Invalid() {
        // Given
        companyAccount.setEmail("notanemail");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }

    @Test
    void testEmail_ValidFormats() {
        // Test various valid email formats
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com"
        };

        for (String email : validEmails) {
            companyAccount.setEmail(email);
            Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);
            assertTrue(violations.isEmpty(), "Email " + email + " should be valid");
        }
    }

    @Test
    void testPasswordHash_Blank_Invalid() {
        // Given
        companyAccount.setPasswordHash("");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password hash is required")));
    }

    @Test
    void testFirstName_TooLong_Invalid() {
        // Given
        companyAccount.setFirstName("a".repeat(101));

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("First name must not exceed 100 characters")));
    }

    @Test
    void testFirstName_MaxLength_Valid() {
        // Given
        companyAccount.setFirstName("a".repeat(100));

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFirstName_Null_Valid() {
        // Given
        companyAccount.setFirstName(null);

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertTrue(violations.isEmpty(), "First name is optional");
    }

    @Test
    void testLastName_TooLong_Invalid() {
        // Given
        companyAccount.setLastName("a".repeat(101));

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Last name must not exceed 100 characters")));
    }

    @Test
    void testVerificationCode_InvalidLength_Invalid() {
        // Given
        companyAccount.setVerificationCode("123");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("exactly 4 characters")));
    }

    @Test
    void testVerificationCode_ValidLength() {
        // Given
        companyAccount.setVerificationCode("1234");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLoginAttempts_Negative_Invalid() {
        // Given
        companyAccount.setLoginAttempts(-1);

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Login attempts cannot be negative")));
    }

    @Test
    void testVerifyAttempts_Negative_Invalid() {
        // Given
        companyAccount.setVerifyAttempts(-1);

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Verify attempts cannot be negative")));
    }

    @Test
    void testAccountStatus_InvalidValue_Invalid() {
        // Given
        companyAccount.setAccountStatus("INVALID_STATUS");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("ACTIVE, BLOCKED, PENDING, or SUSPENDED")));
    }

    @Test
    void testAccountStatus_ValidValues() {
        // Test all valid status values
        String[] validStatuses = {"ACTIVE", "BLOCKED", "PENDING", "SUSPENDED"};

        for (String status : validStatuses) {
            companyAccount.setAccountStatus(status);
            Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);
            assertTrue(violations.isEmpty(), "Status " + status + " should be valid");
        }
    }

    @Test
    void testMultipleViolations() {
        // Given - multiple invalid fields
        companyAccount.setUsername("ab");
        companyAccount.setEmail("invalid-email");
        companyAccount.setPasswordHash("");

        // When
        Set<ConstraintViolation<CompanyAccount>> violations = validator.validate(companyAccount);

        // Then
        assertTrue(violations.size() >= 3, "Should have at least 3 violations");
    }
}
