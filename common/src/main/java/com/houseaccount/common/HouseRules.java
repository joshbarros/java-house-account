package com.houseaccount.common;

import java.util.Locale;

public final class HouseRules {
    private HouseRules() {
    }

    public static boolean blocked(String region) {
        return region != null && region.toLowerCase(Locale.ROOT).contains("blocked");
    }

    public static boolean exempt(String region) {
        return region != null && region.toLowerCase(Locale.ROOT).contains("exempt");
    }

    public static int taxOf(int goodsCents) {
        return Math.toIntExact((long) goodsCents * 8 / 100);
    }

    public static int total(int goodsCents, int taxCents) {
        return Math.addExact(goodsCents, taxCents);
    }

    public static boolean closed(String warehouseCode) {
        return warehouseCode != null && warehouseCode.toLowerCase(Locale.ROOT).contains("closed");
    }

    public static boolean shipDeclined(String shipToken) {
        return shipToken != null && shipToken.endsWith("0000");
    }

    public static boolean bounce(String email) {
        return email != null && email.toLowerCase(Locale.ROOT).endsWith("@bounce.test");
    }

    public static boolean releaseCredit(String status) {
        return switch (status) {
            case "CREDIT_HELD", "ALLOCATED", "INVOICED", "POSTED", "PICKED" -> true;
            default -> false;
        };
    }

    public static boolean releaseStock(String status) {
        return switch (status) {
            case "ALLOCATED", "INVOICED", "POSTED", "PICKED" -> true;
            default -> false;
        };
    }

    public static boolean voidInvoice(String status) {
        return switch (status) {
            case "INVOICED", "POSTED", "PICKED" -> true;
            default -> false;
        };
    }

    public static boolean reverseLedger(String status) {
        return switch (status) {
            case "POSTED", "PICKED" -> true;
            default -> false;
        };
    }

    public static boolean cancelPick(String status) {
        return "PICKED".equals(status);
    }
}
