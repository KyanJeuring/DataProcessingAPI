package com.dataprocessingapi.databaseObjects;

import java.time.LocalDate;

public class Licence {
    private LicenceType licenceType;
    private LocalDate activationDate;
    private LocalDate expirationDate;
    private boolean hasDiscount;

    public Licence(LicenceType licenceType, LocalDate activationDate, LocalDate expirationDate) {
        this.licenceType = licenceType;
        this.activationDate = activationDate;
        this.expirationDate = expirationDate;
    }

    public LicenceType getLicenceType() {
        return this.licenceType;
    }

    public void setLicenceType(LicenceType licenceType) {
        this.licenceType = licenceType;
    }

    public LocalDate getActivationDate() {
        return this.activationDate;
    }

    public void setActivationDate(LocalDate activationDate) {
        this.activationDate = activationDate;
    }

    public LocalDate getExpirationDate() {
        return this.expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isHasDiscount() {
        return this.hasDiscount;
    }

    public void setHasDiscount(boolean hasDiscount) {
        this.hasDiscount = hasDiscount;
    }
}
