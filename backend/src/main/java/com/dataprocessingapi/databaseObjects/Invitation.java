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
        this.inviter = inviter;
        this.validityPeriod = validityPeriod;
    }

    public CompanyAccount getInviter() {
        return this.inviter;
    }

    public void setInviter(CompanyAccount inviter) {
        this.inviter = inviter;
    }

    public CompanyAccount getInvitee() {
        return this.invitee;
    }

    public void setInvitee(CompanyAccount invitee) {
        this.invitee = invitee;
    }

    public LocalDate getSentDate() {
        return this.sentDate;
    }

    public void setSentDate(LocalDate sentDate) {
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
        this.validityPeriod = validityPeriod;
    }
}
