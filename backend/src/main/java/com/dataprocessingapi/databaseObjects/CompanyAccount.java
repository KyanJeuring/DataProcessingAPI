package com.dataprocessingapi.databaseObjects;

import java.util.HashSet;

public class CompanyAccount {
    private String emailAddress;
    private String password;
    private HashSet<Profile> profiles;
    private boolean isVerified;
    private boolean isBlocked;

    public CompanyAccount(String emailAddress, String password) {
        this.emailAddress = emailAddress;
        this.password = password;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashSet<Profile> getProfiles() {
        return this.profiles;
    }

    public void setProfiles(HashSet<Profile> profiles) {
        this.profiles = profiles;
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
