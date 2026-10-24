package com.houseaccount.common;

public record InvoiceIssueCommand(String orderId, String customerId, int totalCents) {
}
