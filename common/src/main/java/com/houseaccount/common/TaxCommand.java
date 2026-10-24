package com.houseaccount.common;

public record TaxCommand(String orderId, String region, int goodsCents) {
}
