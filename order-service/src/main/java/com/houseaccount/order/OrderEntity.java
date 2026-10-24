package com.houseaccount.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales_orders")
public class OrderEntity {
    @Id
    private String id;
    @Column(name = "request_id", unique = true)
    private String requestId;
    @Column(name = "customer_id")
    private String customerId;
    private String email;
    private String sku;
    private int quantity;
    private String region;
    @Column(name = "ship_token")
    private String shipToken;
    @Column(name = "warehouse_code")
    private String warehouseCode;
    @Column(name = "unit_price_cents")
    private int unitPriceCents;
    @Column(name = "goods_cents")
    private int goodsCents;
    @Column(name = "tax_cents")
    private int taxCents;
    @Column(name = "total_cents")
    private int totalCents;
    private String status;
    @Column(name = "notice_status")
    private String noticeStatus;
    private String reason;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getShipToken() { return shipToken; }
    public void setShipToken(String shipToken) { this.shipToken = shipToken; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public int getUnitPriceCents() { return unitPriceCents; }
    public void setUnitPriceCents(int unitPriceCents) { this.unitPriceCents = unitPriceCents; }
    public int getGoodsCents() { return goodsCents; }
    public void setGoodsCents(int goodsCents) { this.goodsCents = goodsCents; }
    public int getTaxCents() { return taxCents; }
    public void setTaxCents(int taxCents) { this.taxCents = taxCents; }
    public int getTotalCents() { return totalCents; }
    public void setTotalCents(int totalCents) { this.totalCents = totalCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNoticeStatus() { return noticeStatus; }
    public void setNoticeStatus(String noticeStatus) { this.noticeStatus = noticeStatus; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
