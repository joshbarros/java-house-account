package com.houseaccount.party;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id
    private String id;
    private String name;
    private String email;
    @Column(name = "credit_limit_cents")
    private int creditLimitCents;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getCreditLimitCents() { return creditLimitCents; }
    public void setCreditLimitCents(int creditLimitCents) { this.creditLimitCents = creditLimitCents; }
}
