package com.houseaccount.common;

public final class Channels {
    public static final String CUSTOMERS = "house.customers";
    public static final String CATALOG = "house.catalog";
    public static final String PRICES = "house.prices";
    public static final String TAX = "house.tax";
    public static final String CREDIT = "house.credit";
    public static final String STOCK = "house.stock";
    public static final String INVOICES = "house.invoices";
    public static final String LEDGER = "house.ledger";
    public static final String FULFILLMENT = "house.fulfillment";
    public static final String SHIPMENTS = "house.shipments";
    public static final String NOTICES = "house.notices";
    public static final String ORDERS = "house.orders";

    public static final String LOCATE = "house.catalog.locate";
    public static final String LOCATE_DLQ = "house.catalog.locate.dlq";
    public static final String PRICE = "house.price.quote";
    public static final String PRICE_DLQ = "house.price.quote.dlq";
    public static final String TAX_ASSESS = "house.tax.assess";
    public static final String TAX_DLQ = "house.tax.assess.dlq";
    public static final String CREDIT_RESERVE = "house.credit.reserve";
    public static final String CREDIT_RESERVE_DLQ = "house.credit.reserve.dlq";
    public static final String CREDIT_RELEASE = "house.credit.release";
    public static final String CREDIT_RELEASE_DLQ = "house.credit.release.dlq";
    public static final String STOCK_RESERVE = "house.stock.reserve";
    public static final String STOCK_RESERVE_DLQ = "house.stock.reserve.dlq";
    public static final String STOCK_RELEASE = "house.stock.release";
    public static final String STOCK_RELEASE_DLQ = "house.stock.release.dlq";
    public static final String INVOICE_ISSUE = "house.invoice.issue";
    public static final String INVOICE_ISSUE_DLQ = "house.invoice.issue.dlq";
    public static final String INVOICE_VOID = "house.invoice.void";
    public static final String INVOICE_VOID_DLQ = "house.invoice.void.dlq";
    public static final String LEDGER_POST = "house.ledger.post";
    public static final String LEDGER_POST_DLQ = "house.ledger.post.dlq";
    public static final String LEDGER_REVERSE = "house.ledger.reverse";
    public static final String LEDGER_REVERSE_DLQ = "house.ledger.reverse.dlq";
    public static final String PICK = "house.pick";
    public static final String PICK_DLQ = "house.pick.dlq";
    public static final String PICK_CANCEL = "house.pick.cancel";
    public static final String PICK_CANCEL_DLQ = "house.pick.cancel.dlq";
    public static final String SHIP = "house.ship";
    public static final String SHIP_DLQ = "house.ship.dlq";
    public static final String NOTICE = "house.notify";
    public static final String NOTICE_DLQ = "house.notify.dlq";

    private Channels() {
    }
}
