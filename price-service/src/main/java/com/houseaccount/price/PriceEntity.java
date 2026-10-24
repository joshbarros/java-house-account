package com.houseaccount.price;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prices")
public class PriceEntity {
    @Id
    private String sku;
    @Column(name = "unit_price_cents")
    private int unitPriceCents;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getUnitPriceCents() { return unitPriceCents; }
    public void setUnitPriceCents(int unitPriceCents) { this.unitPriceCents = unitPriceCents; }
}
