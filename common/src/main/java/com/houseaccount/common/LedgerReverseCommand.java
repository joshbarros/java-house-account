package com.houseaccount.common;

public record LedgerReverseCommand(String orderId, String customerId, int amountCents) {
}
