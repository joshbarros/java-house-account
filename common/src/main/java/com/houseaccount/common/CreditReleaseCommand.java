package com.houseaccount.common;

public record CreditReleaseCommand(String orderId, String customerId, int amountCents) {
}
