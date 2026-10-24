package com.houseaccount.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "receivables")
public class ReceivableEntity {
    @Id
    @Column(name = "customer_id")
    private String customerId;
    @Column(name = "balance_cents")
    private int balanceCents;

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public int getBalanceCents() { return balanceCents; }
    public void setBalanceCents(int balanceCents) { this.balanceCents = balanceCents; }
}
