package com.houseaccount.credit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_accounts")
public class AccountEntity {
    @Id
    private String id;
    @Column(name = "credit_limit_cents")
    private int creditLimitCents;
    @Column(name = "exposed_cents")
    private int exposedCents;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getCreditLimitCents() { return creditLimitCents; }
    public void setCreditLimitCents(int creditLimitCents) { this.creditLimitCents = creditLimitCents; }
    public int getExposedCents() { return exposedCents; }
    public void setExposedCents(int exposedCents) { this.exposedCents = exposedCents; }
}
