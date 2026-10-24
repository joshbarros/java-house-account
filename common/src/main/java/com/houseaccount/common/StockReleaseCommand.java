package com.houseaccount.common;

public record StockReleaseCommand(String orderId, String sku, int quantity) {
}
