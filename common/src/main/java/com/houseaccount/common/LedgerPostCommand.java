package com.houseaccount.common;

public record LedgerPostCommand(String orderId, String customerId, int amountCents) {
}
