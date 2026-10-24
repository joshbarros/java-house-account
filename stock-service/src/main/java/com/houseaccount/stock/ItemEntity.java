package com.houseaccount.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_items")
public class ItemEntity {
    @Id
    private String sku;
    @Column(name = "on_hand")
    private int onHand;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getOnHand() { return onHand; }
    public void setOnHand(int onHand) { this.onHand = onHand; }
}
