package com.houseaccount.common;

public record CreditReserveCommand(String orderId, String customerId, int amountCents) {
}
