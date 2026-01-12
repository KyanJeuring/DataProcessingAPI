package main.java.com.dataprocessingapi.databaseObjects;

import java.time.LocalDate;

public class Licence {
    private LicenceType licenceType;
    private LocalDate activationDate;
    private LocalDate expirationDate;
    private boolean hasDiscount;

    public Licence(LicenceType licenceType, LocalDate activationDate, LocalDate expirationDate) {
        this.setLicenceType(licenceType);
        this.setActivationDate(activationDate);
        this.setExpirationDate(expirationDate);
    }

    public LicenceType getLicenceType() {
        return this.licenceType;
    }

    public void setLicenceType(LicenceType licenceType) {
        if (licenceType == null) {
            throw new IllegalArgumentException("Please provide a license type object.");
        }

        this.licenceType = licenceType;
    }

    public LocalDate getActivationDate() {
        return this.activationDate;
    }

    public void setActivationDate(LocalDate activationDate) {
        if (activationDate == null) {
            throw new IllegalArgumentException("Please provide an appropriate activation date.");
        }

        this.activationDate = activationDate;
    }

    public LocalDate getExpirationDate() {
        return this.expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        if (expirationDate == null) {
            throw new IllegalArgumentException("Please provide an appropriate expiration date.");
        }

        this.expirationDate = expirationDate;
    }

    public boolean isHasDiscount() {
        return this.hasDiscount;
    }

    public void setHasDiscount(boolean hasDiscount) {
        this.hasDiscount = hasDiscount;
    }
}
