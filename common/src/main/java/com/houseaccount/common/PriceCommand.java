package com.houseaccount.common;

public record PriceCommand(String orderId, String sku, int quantity) {
}
