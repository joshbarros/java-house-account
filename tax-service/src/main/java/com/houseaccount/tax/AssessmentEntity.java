package com.houseaccount.tax;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessments")
public class AssessmentEntity {
    @Id
    @Column(name = "order_id")
    private String orderId;
    @Column(name = "tax_cents")
    private int taxCents;
    @Column(name = "total_cents")
    private int totalCents;
    private String status;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public int getTaxCents() { return taxCents; }
    public void setTaxCents(int taxCents) { this.taxCents = taxCents; }
    public int getTotalCents() { return totalCents; }
    public void setTotalCents(int totalCents) { this.totalCents = totalCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
