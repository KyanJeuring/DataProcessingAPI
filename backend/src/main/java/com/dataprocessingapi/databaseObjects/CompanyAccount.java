package main.java.com.dataprocessingapi.databaseObjects;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompanyAccount {
    private String emailAddress;
    private String password;
    private HashSet<Profile> profiles;
    private boolean isVerified;
    private boolean isBlocked;

    public CompanyAccount(String emailAddress, String password) {
        this.setEmailAddress(emailAddress);
        this.setPassword(password);
        this.profiles = new HashSet<>();
        this.isVerified = false;
        this.isBlocked = false;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(emailAddress);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + emailAddress);
        }

        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }

        this.password = password;
    }

    public HashSet<Profile> getProfiles() {
        return this.profiles;
    }

    public void addProfile(Profile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Please provide a valid profile.");
        }

        this.profiles.add(profile);
    }

    public boolean isVerified() {
        return this.isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isBlocked() {
        return this.isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
