package main.java.com.dataprocessingapi.databaseObjects;

import java.time.LocalDate;

public class Invitation {
    private CompanyAccount inviter;
    private CompanyAccount invitee;
    private LocalDate sentDate;
    private boolean activated;
    private boolean discountApplied;
    private int validityPeriod;

    public Invitation(CompanyAccount inviter, int validityPeriod) {
        this.setInviter(inviter);
        this.setValidityPeriod(validityPeriod);
    }

    public CompanyAccount getInviter() {
        return this.inviter;
    }

    public void setInviter(CompanyAccount inviter) {
        if (inviter == null) {
            throw new IllegalArgumentException("Please provide an company account object.");
        }

        this.inviter = inviter;
    }

    public CompanyAccount getInvitee() {
        return this.invitee;
    }

    public void setInvitee(CompanyAccount invitee) {
        if (invitee == null) {
            throw new IllegalArgumentException("Please provide a company account object.");
        }

        this.invitee = invitee;
    }

    public LocalDate getSentDate() {
        return this.sentDate;
    }

    public void setSentDate(LocalDate sentDate) {
        if (sentDate == null) {
            throw new IllegalArgumentException("sentDate cannot be null");
        }

        if (sentDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "sentDate cannot be in the future: " + sentDate
            );
        }
        
        this.sentDate = sentDate;
    }

    public boolean isActivated() {
        return this.activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isDiscountApplied() {
        return this.discountApplied;
    }

    public void setDiscountApplied(boolean discountApplied) {
        this.discountApplied = discountApplied;
    }

    public int getValidityPeriod() {
        return this.validityPeriod;
    }

    public void setValidityPeriod(int validityPeriod) {
        if (validityPeriod < 1) {
            throw new IllegalArgumentException("The invitation must be valid for at least 1 day.");
        }

        this.validityPeriod = validityPeriod;
    }
}
