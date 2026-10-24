package com.houseaccount.fulfillment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "picks")
public class PickEntity {
    @Id
    @Column(name = "order_id")
    private String orderId;
    @Column(name = "warehouse_code")
    private String warehouseCode;
    private String status;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
