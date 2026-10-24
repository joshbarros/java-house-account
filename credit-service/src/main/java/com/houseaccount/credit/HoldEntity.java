package com.houseaccount.credit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_holds")
public class HoldEntity {
    @Id
    @Column(name = "order_id")
    private String orderId;
    @Column(name = "customer_id")
    private String customerId;
    @Column(name = "amount_cents")
    private int amountCents;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
}
