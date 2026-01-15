package com.fleetmaster.controllers;

import com.fleetmaster.dtos.ApiLoginDto;
import com.fleetmaster.dtos.ApiRegisterDto;
import com.fleetmaster.dtos.EmailDto;
import com.fleetmaster.dtos.LoginDto;
import com.fleetmaster.dtos.RegisterDto;
import com.fleetmaster.dtos.VerifyCodeDto;
import com.fleetmaster.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and verification")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new company account", description = "Creates a new company account and sends a verification code via email.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterDto dto) {
        authService.register(dto);
        return ResponseEntity.ok("Company account registered. Verification code sent.");
    }

    @Operation(summary = "Login to company account", description = "Authenticates a user and returns a JWT token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDto dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok().body(java.util.Map.of("token", token));
    }

    @Operation(summary = "Register API Account", description = "Register a new API user account (for external systems).")
    @PostMapping("/api-register")
    public ResponseEntity<Object> apiRegister(@RequestBody ApiRegisterDto dto) {
        authService.registerApiAccount(dto.getUsername(), dto.getPassword());
        return ResponseEntity.ok("API Account registered.");
    }

    @Operation(summary = "Login API Account", description = "Login as an API user to get a JWT token.")
    @PostMapping("/api-login")
    public ResponseEntity<Object> apiLogin(@RequestBody ApiLoginDto dto) {
        String token = authService.loginApiAccount(dto.getUsername(), dto.getPassword());
        return ResponseEntity.ok().body(java.util.Map.of("token", token));
    }

    @Operation(summary = "Send verification code", description = "Resends the verification code to the registered email.")
    @ApiResponse(responseCode = "200", description = "Verification code sent")
    @PostMapping("/verify/code/send")
    public ResponseEntity<Object> sendCode(@RequestBody EmailDto emailDto) {
        authService.sendVerifyCode(emailDto.getEmail());
        return ResponseEntity.ok("Verification code sent.");
    }

    @Operation(summary = "Check verification code", description = "Verifies the email using the code sent.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification successful"),
        @ApiResponse(responseCode = "400", description = "Invalid code")
    })
    @PostMapping("/verify/code/check")
    public ResponseEntity<Object> checkCode(@RequestBody VerifyCodeDto dto) {
        authService.checkVerifyCode(dto);
        return ResponseEntity.ok("Verification checked.");
    }

    @Operation(summary = "Get current user info", description = "Returns details of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "User details retrieved")
    @GetMapping("/me")
    public ResponseEntity<Object> me(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(authentication.getPrincipal());
    }
}
