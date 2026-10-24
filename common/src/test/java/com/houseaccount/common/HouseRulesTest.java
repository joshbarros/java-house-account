package com.houseaccount.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HouseRulesTest {

    @Test
    void taxIsEightPercentTruncatedTowardZero() {
        assertEquals(200, HouseRules.taxOf(2500));
        assertEquals(2700, HouseRules.total(2500, 200));
        assertEquals(0, HouseRules.taxOf(1));
    }

    @Test
    void blockedWinsOverExemptAndANormalRegionIsTaxed() {
        assertTrue(HouseRules.blocked("blocked-exempt"));
        assertTrue(HouseRules.exempt("EXEMPT-ZONE"));
        assertFalse(HouseRules.blocked("SP"));
        assertFalse(HouseRules.exempt("SP"));
    }

    @Test
    void closedWarehouseDeclinedShipAndBounceAreExact() {
        assertTrue(HouseRules.closed("WH-CLOSED"));
        assertFalse(HouseRules.closed("CENTRAL"));
        assertTrue(HouseRules.shipDeclined("DOCK0000"));
        assertFalse(HouseRules.shipDeclined("DOCK0001"));
        assertTrue(HouseRules.bounce("A@Bounce.Test"));
        assertFalse(HouseRules.bounce("ada@shop.test"));
    }

    @Test
    void compensationFollowsTheStepsThatAlreadyCommitted() {
        assertFalse(HouseRules.releaseCredit("TAXED"));
        assertTrue(HouseRules.releaseCredit("CREDIT_HELD"));
        assertFalse(HouseRules.releaseStock("CREDIT_HELD"));
        assertTrue(HouseRules.releaseStock("ALLOCATED"));
        assertFalse(HouseRules.voidInvoice("ALLOCATED"));
        assertTrue(HouseRules.voidInvoice("INVOICED"));
        assertFalse(HouseRules.reverseLedger("INVOICED"));
        assertTrue(HouseRules.reverseLedger("POSTED"));
        assertFalse(HouseRules.cancelPick("POSTED"));
        assertTrue(HouseRules.cancelPick("PICKED"));
        assertTrue(HouseRules.releaseCredit("PICKED"));
        assertTrue(HouseRules.releaseStock("PICKED"));
        assertTrue(HouseRules.voidInvoice("PICKED"));
        assertTrue(HouseRules.reverseLedger("PICKED"));
    }
}
