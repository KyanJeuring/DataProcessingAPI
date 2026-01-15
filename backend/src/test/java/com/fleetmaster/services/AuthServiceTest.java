package com.fleetmaster.services;

import com.fleetmaster.dtos.LoginDto;
import com.fleetmaster.dtos.RegisterDto;
import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.exceptions.BusinessException;
import com.fleetmaster.repositories.CompanyAccountRepository;
import com.fleetmaster.security.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private CompanyAccountRepository companyAccountRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AuthService authService;

    private RegisterDto registerDto;
    private LoginDto loginDto;
    private CompanyAccount companyAccount;

    @BeforeEach
    void setUp() {
        // Manually inject the mocked EntityManager using reflection
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("entityManager");
            field.setAccessible(true);
            field.set(authService, entityManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Mock the native queries for company creation (lenient for tests that don't use it)
        Query companyQuery = mock(Query.class);
        lenient().when(entityManager.createNativeQuery(contains("sp_register_company")))
                .thenReturn(companyQuery);
        lenient().when(companyQuery.setParameter(anyString(), any())).thenReturn(companyQuery);
        lenient().when(companyQuery.getSingleResult()).thenReturn(1L); // Return company ID
        
        registerDto = new RegisterDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");
        registerDto.setUsername("testuser");
        registerDto.setCompanyName("Test Company");

        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        companyAccount = new CompanyAccount();
        companyAccount.setId(1L);
        companyAccount.setEmail("test@example.com");
        companyAccount.setUsername("testuser");
        companyAccount.setPasswordHash("$2a$10$hashedpassword");
        companyAccount.setVerified(true);
        companyAccount.setAccountStatus("ACTIVE");
        companyAccount.setLoginAttempts(0);
    }

    @Test
    void testRegister_Success() {
        // Given
        when(companyAccountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");

        // When
        assertDoesNotThrow(() -> authService.register(registerDto));

        // Then
        verify(companyAccountRepository, times(1)).save(any(CompanyAccount.class));
        verify(emailService, times(1)).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Given
        when(companyAccountRepository.findByEmail(anyString())).thenReturn(Optional.of(companyAccount));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerDto);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(companyAccountRepository, never()).save(any(CompanyAccount.class));
    }

    @Test
    void testLogin_Success() {
        // Given
        when(companyAccountRepository.findByEmail(anyString())).thenReturn(Optional.of(companyAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        // When
        String token = authService.login(loginDto);

        // Then
        assertNotNull(token);
        assertEquals("mock-jwt-token", token);
        verify(companyAccountRepository, times(1)).save(any(CompanyAccount.class));
    }

    @Test
    void testLogin_EmailNotVerified() {
        // Given
        companyAccount.setVerified(false);
        when(companyAccountRepository.findByEmail(anyString())).thenReturn(Optional.of(companyAccount));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals("Email not verified", exception.getMessage());
    }

    @Test
    void testLogin_AccountBlocked() {
        // Given
        companyAccount.setAccountStatus("BLOCKED");
        when(companyAccountRepository.findByEmail(anyString())).thenReturn(Optional.of(companyAccount));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals("Account blocked", exception.getMessage());
    }

    @Test
    void testLogin_IncorrectPassword() {
        // Given
        when(companyAccountRepository.findByEmail(anyString())).thenReturn(Optional.of(companyAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals("Incorrect password", exception.getMessage());
        verify(companyAccountRepository, times(1)).save(any(CompanyAccount.class));
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        when(companyAccountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals("CompanyAccount not found", exception.getMessage());
    }
}
