package com.fleetmaster.services;

import com.fleetmaster.dtos.LoginDto;
import com.fleetmaster.dtos.RegisterDto;
import com.fleetmaster.dtos.VerifyCodeDto;
import com.fleetmaster.entities.User;
import com.fleetmaster.repositories.UserRepository;
import com.fleetmaster.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository repo, EmailService emailService, JwtUtil jwtUtil) {
        this.userRepository = repo;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
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

        if ("BLOCKED".equals(user.getStatus())) {
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

        if (user.getVerificationCode().equals(dto.getCode())) {
            user.setVerified(true);
            user.setVerifyAttempts(0);
            user.setVerificationCode(null); // clear the code
        } else {
            user.setVerifyAttempts(user.getVerifyAttempts() + 1);
            if (user.getVerifyAttempts() >= 3) {
                user.setStatus("BLOCKED");
            }
        }

        userRepository.save(user);
    }

    private String generateVerificationCode() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
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

        if (!user.getPassword().equals(dto.getPassword())) {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            if (user.getLoginAttempts() >= 3) {
                user.setStatus("BLOCKED");
            }
            userRepository.save(user);
            throw new RuntimeException("Incorrect password");
        }

        user.setLoginAttempts(0);
        userRepository.save(user);

        return jwtUtil.generateToken(user.getEmail());
    }
}
