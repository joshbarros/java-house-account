package com.houseaccount.common;

public record StockReserveCommand(String orderId, String sku, int quantity) {
}
