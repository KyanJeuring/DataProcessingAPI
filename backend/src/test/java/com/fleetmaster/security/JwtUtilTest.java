package com.fleetmaster.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateToken_WithSubjectOnly() {
        // Given
        String subject = "test@example.com";

        // When
        String token = jwtUtil.generateToken(subject);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token contains subject and default type
        String extractedSubject = jwtUtil.extractSubject(token);
        String extractedType = jwtUtil.extractType(token);
        
        assertEquals(subject, extractedSubject);
        assertEquals("COMPANY", extractedType);
    }

    @Test
    void testGenerateToken_WithSubjectAndType() {
        // Given
        String subject = "apiuser";
        String type = "API";

        // When
        String token = jwtUtil.generateToken(subject, type);

        // Then
        assertNotNull(token);
        
        String extractedSubject = jwtUtil.extractSubject(token);
        String extractedType = jwtUtil.extractType(token);
        
        assertEquals(subject, extractedSubject);
        assertEquals(type, extractedType);
    }

    @Test
    void testExtractSubject() {
        // Given
        String subject = "user@example.com";
        String token = jwtUtil.generateToken(subject);

        // When
        String extractedSubject = jwtUtil.extractSubject(token);

        // Then
        assertEquals(subject, extractedSubject);
    }

    @Test
    void testExtractEmail_BackwardCompatibility() {
        // Given
        String email = "user@example.com";
        String token = jwtUtil.generateToken(email);

        // When
        String extractedEmail = jwtUtil.extractEmail(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void testExtractType() {
        // Given
        String subject = "testuser";
        String type = "ADMIN";
        String token = jwtUtil.generateToken(subject, type);

        // When
        String extractedType = jwtUtil.extractType(token);

        // Then
        assertEquals(type, extractedType);
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        String subject = "valid@example.com";
        String token = jwtUtil.generateToken(subject);

        // When
        boolean isValid = jwtUtil.validateToken(token, subject);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidSubject() {
        // Given
        String subject = "user@example.com";
        String wrongSubject = "wrong@example.com";
        String token = jwtUtil.generateToken(subject);

        // When
        boolean isValid = jwtUtil.validateToken(token, wrongSubject);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testTokenContainsIssuedAtAndExpiration() {
        // Given
        String subject = "test@example.com";

        // When
        String token = jwtUtil.generateToken(subject);

        // Then
        // Parse token to verify claims
        String SECRET = "fleetmastersecretfhsdifgsduzfktsdufgsdt34zu2gwjhdsjhfdscjym";
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().getTime() > claims.getIssuedAt().getTime());
    }

    @Test
    void testGenerateMultipleTokens_UniqueTokens() {
        // Given
        String subject = "user@example.com";

        // When
        String token1 = jwtUtil.generateToken(subject);
        
        // Wait a full second to ensure different timestamp
        try {
            Thread.sleep(1001);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtUtil.generateToken(subject);

        // Then
        assertNotEquals(token1, token2, "Tokens generated at different times should be unique");
        
        // Both should still be valid
        assertTrue(jwtUtil.validateToken(token1, subject));
        assertTrue(jwtUtil.validateToken(token2, subject));
    }
}
