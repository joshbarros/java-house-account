package com.houseaccount.common;

public record TaxAssessed(String orderId, int taxCents, int totalCents, String status) {
}
