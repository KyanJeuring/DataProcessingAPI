package com.fleetmaster.services;

import com.fleetmaster.dtos.LoginDto;
import com.fleetmaster.dtos.RegisterDto;
import com.fleetmaster.dtos.VerifyCodeDto;
import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.entities.ApiAccount;
import com.fleetmaster.exceptions.BusinessException;
import com.fleetmaster.repositories.CompanyAccountRepository;
import com.fleetmaster.repositories.ApiAccountRepository;
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

    private static final String ACCOUNT_BLOCKED = "BLOCKED";
    private static final String ACCOUNT_NOT_FOUND_MSG = "CompanyAccount not found";

    private final CompanyAccountRepository companyAccountRepository;
    private final ApiAccountRepository apiAccountRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    private final Random random = new SecureRandom();

    public AuthService(
            CompanyAccountRepository repo,
            ApiAccountRepository apiAccountRepository,
            EmailService emailService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder
    ) {
        this.companyAccountRepository = repo;
        this.apiAccountRepository = apiAccountRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterDto dto) {
        if (companyAccountRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException("Email already exists");
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
                
                if (result instanceof Number number) {
                    companyId = number.longValue();
                }
            } catch (Exception e) {
                // If company exists or other error
                throw new BusinessException("Error creating company: " + e.getMessage());
            }
        }

        CompanyAccount companyAccount = new CompanyAccount();
        companyAccount.setUsername(dto.getUsername()); 
        companyAccount.setEmail(dto.getEmail());
        companyAccount.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        companyAccount.setFirstName(dto.getFirstName());
        companyAccount.setLastName(dto.getLastName());
        companyAccount.setRoles(dto.getRoles());                    
        companyAccount.setPreferences(dto.getPreferences());

        companyAccount.setAccountStatus("ACTIVE");
        companyAccount.setVerified(false);
        companyAccount.setCompanyId(companyId);

        companyAccount.setLoginAttempts(0);
        companyAccount.setVerifyAttempts(0);

        String code = generateVerificationCode();
        companyAccount.setVerificationCode(code);

        companyAccountRepository.save(companyAccount);
        emailService.sendVerificationCode(dto.getEmail(), code);
    }

    public void sendVerifyCode(String email) {
        CompanyAccount companyAccount = companyAccountRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND_MSG));

        if (ACCOUNT_BLOCKED.equals(companyAccount.getAccountStatus())) { 
            throw new BusinessException("CompanyAccount is blocked");
        }

        String code = generateVerificationCode();
        companyAccount.setVerificationCode(code);

        companyAccount.setVerifyAttempts(0);

        companyAccountRepository.save(companyAccount);
        emailService.sendVerificationCode(email, code);
    }

    public void checkVerifyCode(VerifyCodeDto dto) {
        CompanyAccount companyAccount = companyAccountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND_MSG));

        if (ACCOUNT_BLOCKED.equals(companyAccount.getAccountStatus())) {
            throw new BusinessException("CompanyAccount is blocked");
        }
        
        String savedCode = companyAccount.getVerificationCode();
        if (savedCode != null && savedCode.equals(dto.getCode())) {
            companyAccount.setVerified(true);
            companyAccount.setVerifyAttempts(0);
            companyAccount.setVerificationCode(null); 
        } else {
            companyAccount.setVerifyAttempts(companyAccount.getVerifyAttempts() + 1);
            if (companyAccount.getVerifyAttempts() >= 3) {
                companyAccount.setAccountStatus(ACCOUNT_BLOCKED);
            }
        }

        companyAccountRepository.save(companyAccount);
    }

    public String login(LoginDto dto) {
        CompanyAccount companyAccount = companyAccountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND_MSG));

        if (!companyAccount.isVerified()) {
            throw new BusinessException("Email not verified");
        }

        if (ACCOUNT_BLOCKED.equals(companyAccount.getAccountStatus())) {
            throw new BusinessException("Account blocked");
        }

        // Check temp lock
        if (companyAccount.getLockedUntil() != null && companyAccount.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Account temporarily locked until " + companyAccount.getLockedUntil());
        }

        if (!passwordEncoder.matches(dto.getPassword(), companyAccount.getPasswordHash())) {
            companyAccount.setLoginAttempts(companyAccount.getLoginAttempts() + 1);
            if (companyAccount.getLoginAttempts() >= 3) {
                // Lock for 15 minutes
                companyAccount.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                companyAccount.setLoginAttempts(0);
            }
            companyAccountRepository.save(companyAccount);
            throw new BusinessException("Incorrect password");
        }

        companyAccount.setLoginAttempts(0);
        companyAccount.setLockedUntil(null);
        companyAccountRepository.save(companyAccount);

        return jwtUtil.generateToken(companyAccount.getEmail(), "COMPANY");
    }

    public CompanyAccount getCompanyAccountByEmail(String email) {
        return companyAccountRepository.findByEmail(email).orElse(null);
    }

    public ApiAccount getApiAccountByUsername(String username) {
        return apiAccountRepository.findByUsername(username).orElse(null);
    }

    public void registerApiAccount(String username, String password) {
        if (apiAccountRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("Username already exists");
        }
        ApiAccount account = new ApiAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setActive(true);
        apiAccountRepository.save(account);
    }

    public String loginApiAccount(String username, String password) {
        ApiAccount account = apiAccountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("API Account not found"));

        if (!account.isActive()) {
            throw new BusinessException("Account is inactive");
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new BusinessException("Incorrect password");
        }

        return jwtUtil.generateToken(username, "API");
    }


    private String generateVerificationCode() {
        return String.format("%04d", random.nextInt(10000));
    }
}
