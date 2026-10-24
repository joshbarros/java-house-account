package com.houseaccount.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoices")
public class InvoiceEntity {
    @Id
    @Column(name = "order_id")
    private String orderId;
    @Column(name = "customer_id")
    private String customerId;
    @Column(name = "total_cents")
    private int totalCents;
    private String status;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public int getTotalCents() { return totalCents; }
    public void setTotalCents(int totalCents) { this.totalCents = totalCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
