package com.fleetmaster.services;

import com.fleetmaster.dtos.LoginDto;
import com.fleetmaster.dtos.RegisterDto;
import com.fleetmaster.dtos.VerifyCodeDto;
import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.repositories.CompanyAccountRepository;
import com.fleetmaster.security.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final CompanyAccountRepository CompanyAccountRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    private final Random random = new SecureRandom();

    public AuthService(
            CompanyAccountRepository repo,
            EmailService emailService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder
    ) {
        this.CompanyAccountRepository = repo;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterDto dto) {
        if (CompanyAccountRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Long companyId = null;
        if (dto.getCompanyName() != null && !dto.getCompanyName().isBlank()) {
            // Call stored procedure to create company and get ID
            // Function returns table(company_id, subscription_id, ...)
            // We select just the company_id
            try {
                Object result = entityManager.createNativeQuery(
                        "SELECT company_id FROM sp_register_company(:name, 'BASIC', true)")
                        .setParameter("name", dto.getCompanyName())
                        .getSingleResult();
                
                if (result instanceof Number) {
                    companyId = ((Number) result).longValue();
                }
            } catch (Exception e) {
                // If company exists or other error
                throw new RuntimeException("Error creating company: " + e.getMessage());
            }
        }

        CompanyAccount companyAccount = new CompanyAccount();
        companyAccount.setName(dto.getName()); 
        companyAccount.setEmail(dto.getEmail());
        companyAccount.setPassword(passwordEncoder.encode(dto.getPassword()));
        companyAccount.setStatus("ACTIVE");
        companyAccount.setVerified(false);
        companyAccount.setCompanyId(companyId);

        companyAccount.setLoginAttempts(0);
        companyAccount.setVerifyAttempts(0);

        String code = generateVerificationCode();
        companyAccount.setVerificationCode(code);

        CompanyAccountRepository.save(companyAccount);
        emailService.sendVerificationCode(dto.getEmail(), code);
    }

    public void sendVerifyCode(String email) {
        CompanyAccount companyAccount = CompanyAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("CompanyAccount not found"));

        if ("BLOCKED".equals(companyAccount.getStatus())) { // If column is "BLOCKED"
            throw new RuntimeException("CompanyAccount is blocked");
        }

        String code = generateVerificationCode();
        companyAccount.setVerificationCode(code);

        companyAccount.setVerifyAttempts(0);

        CompanyAccountRepository.save(companyAccount);
        emailService.sendVerificationCode(email, code);
    }

    public void checkVerifyCode(VerifyCodeDto dto) {
        CompanyAccount companyAccount = CompanyAccountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("CompanyAccount not found"));

        if ("BLOCKED".equals(companyAccount.getStatus())) {
            throw new RuntimeException("CompanyAccount is blocked");
        }
        
        String savedCode = companyAccount.getVerificationCode();
        if (savedCode != null && savedCode.equals(dto.getCode())) {
            companyAccount.setVerified(true);
            companyAccount.setVerifyAttempts(0);
            companyAccount.setVerificationCode(null); 
        } else {
            companyAccount.setVerifyAttempts(companyAccount.getVerifyAttempts() + 1);
            if (companyAccount.getVerifyAttempts() >= 3) {
                companyAccount.setStatus("BLOCKED");
            }
        }

        CompanyAccountRepository.save(companyAccount);
    }

    public String login(LoginDto dto) {
        CompanyAccount companyAccount = CompanyAccountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("CompanyAccount not found"));

        if (!companyAccount.isVerified()) {
            throw new RuntimeException("Email not verified");
        }

        if ("BLOCKED".equals(companyAccount.getStatus())) {
            throw new RuntimeException("Account blocked");
        }

        // Check temp lock
        if (companyAccount.getLockedUntil() != null && companyAccount.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account temporarily locked until " + companyAccount.getLockedUntil());
        }

        if (!passwordEncoder.matches(dto.getPassword(), companyAccount.getPassword())) {
            companyAccount.setLoginAttempts(companyAccount.getLoginAttempts() + 1);
            if (companyAccount.getLoginAttempts() >= 3) {
                // Lock for 15 minutes
                companyAccount.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                companyAccount.setLoginAttempts(0);
            }
            CompanyAccountRepository.save(companyAccount);
            throw new RuntimeException("Incorrect password");
        }

        companyAccount.setLoginAttempts(0);
        companyAccount.setLockedUntil(null);
        CompanyAccountRepository.save(companyAccount);

        return jwtUtil.generateToken(companyAccount.getEmail());
    }

    public CompanyAccount getCompanyAccountByEmail(String email) {
        return CompanyAccountRepository.findByEmail(email).orElse(null);
    }

    private String generateVerificationCode() {
        return String.format("%04d", random.nextInt(10000));
    }
}
