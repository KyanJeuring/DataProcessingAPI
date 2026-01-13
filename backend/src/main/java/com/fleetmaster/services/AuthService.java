package com.fleetmaster.services;

import com.fleetmaster.dtos.LoginDto;
import com.fleetmaster.dtos.RegisterDto;
import com.fleetmaster.dtos.VerifyCodeDto;
import com.fleetmaster.entities.User;
import com.fleetmaster.repositories.UserRepository;
import com.fleetmaster.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new SecureRandom();

    public AuthService(
            UserRepository repo,
            EmailService emailService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = repo;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(dto.getName()); 
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus("ACTIVE");
        user.setVerified(false);

        user.setLoginAttempts(0);
        user.setVerifyAttempts(0);

        String code = generateVerificationCode();
        user.setVerificationCode(code);

        userRepository.save(user);
        emailService.sendVerificationCode(dto.getEmail(), code);
    }

    public void sendVerifyCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("BLOCKED".equals(user.getStatus())) { // If column is "BLOCKED"
            throw new RuntimeException("User is blocked");
        }

        String code = generateVerificationCode();
        user.setVerificationCode(code);

        user.setVerifyAttempts(0);

        userRepository.save(user);
        emailService.sendVerificationCode(email, code);
    }

    public void checkVerifyCode(VerifyCodeDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("BLOCKED".equals(user.getStatus())) {
            throw new RuntimeException("User is blocked");
        }
        
        String savedCode = user.getVerificationCode();
        if (savedCode != null && savedCode.equals(dto.getCode())) {
            user.setVerified(true);
            user.setVerifyAttempts(0);
            user.setVerificationCode(null); 
        } else {
            user.setVerifyAttempts(user.getVerifyAttempts() + 1);
            if (user.getVerifyAttempts() >= 3) {
                user.setStatus("BLOCKED");
            }
        }

        userRepository.save(user);
    }

    public String login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) {
            throw new RuntimeException("Email not verified");
        }

        if ("BLOCKED".equals(user.getStatus())) {
            throw new RuntimeException("Account blocked");
        }

        // Check temp lock
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account temporarily locked until " + user.getLockedUntil());
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            if (user.getLoginAttempts() >= 3) {
                // Lock for 15 minutes
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                user.setLoginAttempts(0);
            }
            userRepository.save(user);
            throw new RuntimeException("Incorrect password");
        }

        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        return jwtUtil.generateToken(user.getEmail());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    private String generateVerificationCode() {
        return String.format("%04d", random.nextInt(10000));
    }
}
