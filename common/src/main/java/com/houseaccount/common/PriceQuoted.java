package com.houseaccount.common;

public record PriceQuoted(String orderId, int unitPriceCents, int goodsCents, String status) {
}
