package com.fleetmaster.controllers;

import com.fleetmaster.dtos.EmailDto;
import com.fleetmaster.dtos.LoginDto;
import com.fleetmaster.dtos.RegisterDto;
import com.fleetmaster.dtos.VerifyCodeDto;
import com.fleetmaster.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto dto) {
        authService.register(dto);
        return ResponseEntity.ok("User registered. Verification code sent.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok().body(java.util.Map.of("token", token));
    }

    @PostMapping("/verify/code/send")
    public ResponseEntity<?> sendCode(@RequestBody EmailDto emailDto) {
        authService.sendVerifyCode(emailDto.getEmail());
        return ResponseEntity.ok("Verification code sent.");
    }

    @PostMapping("/verify/code/check")
    public ResponseEntity<?> checkCode(@RequestBody VerifyCodeDto dto) {
        authService.checkVerifyCode(dto);
        return ResponseEntity.ok("Verification checked.");
    }
}
