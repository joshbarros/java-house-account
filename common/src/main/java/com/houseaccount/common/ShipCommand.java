package com.houseaccount.common;

public record ShipCommand(String orderId, String shipToken) {
}
