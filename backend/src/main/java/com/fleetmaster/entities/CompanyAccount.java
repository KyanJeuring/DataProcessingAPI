package com.fleetmaster.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_account")
public class CompanyAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "login_attempts", nullable = false)
    private int loginAttempts = 0;

    @Column(name = "verify_attempts", nullable = false)
    private int verifyAttempts = 0;

    @Column(name = "account_status")
    private String status = "ACTIVE"; // ACTIVE or BLOCKED

    @Column(name = "verification_code", length = 4)
    private String verificationCode;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "company_id")
    private Long companyId;

    // Getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public int getLoginAttempts() { return loginAttempts; }
    public void setLoginAttempts(int loginAttempts) { this.loginAttempts = loginAttempts; }
    public int getVerifyAttempts() { return verifyAttempts; }
    public void setVerifyAttempts(int verifyAttempts) { this.verifyAttempts = verifyAttempts; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
}
