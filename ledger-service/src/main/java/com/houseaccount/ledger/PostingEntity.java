package com.houseaccount.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "postings")
public class PostingEntity {
    @Id
    private String id;
    @Column(name = "order_id")
    private String orderId;
    @Column(name = "customer_id")
    private String customerId;
    @Column(name = "amount_cents")
    private int amountCents;
    private String kind;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
}
