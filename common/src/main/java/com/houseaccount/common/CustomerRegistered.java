package com.houseaccount.common;

public record CustomerRegistered(String customerId, String name, String email, int creditLimitCents) {
}
