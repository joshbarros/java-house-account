package com.houseaccount.timeline;

import java.time.Instant;

import com.houseaccount.common.CatalogLocated;
import com.houseaccount.common.Channels;
import com.houseaccount.common.CreditEvent;
import com.houseaccount.common.InvoiceEvent;
import com.houseaccount.common.LedgerEvent;
import com.houseaccount.common.NoticeEvent;
import com.houseaccount.common.OrderFact;
import com.houseaccount.common.PickEvent;
import com.houseaccount.common.PriceQuoted;
import com.houseaccount.common.ShipEvent;
import com.houseaccount.common.StockEvent;
import com.houseaccount.common.TaxAssessed;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TimelineWorker {

    private final TimelineRepository entries;

    public TimelineWorker(TimelineRepository entries) {
        this.entries = entries;
    }

    @KafkaListener(topics = Channels.ORDERS, groupId = "timeline-orders")
    @Transactional
    public void onOrder(OrderFact event) {
        add("OrderFact", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.CATALOG, groupId = "timeline-catalog")
    @Transactional
    public void onCatalog(CatalogLocated event) {
        add("CatalogLocated", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.PRICES, groupId = "timeline-prices")
    @Transactional
    public void onPrice(PriceQuoted event) {
        add("PriceQuoted", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.TAX, groupId = "timeline-tax")
    @Transactional
    public void onTax(TaxAssessed event) {
        add("TaxAssessed", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.CREDIT, groupId = "timeline-credit")
    @Transactional
    public void onCredit(CreditEvent event) {
        add("CreditEvent", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.STOCK, groupId = "timeline-stock")
    @Transactional
    public void onStock(StockEvent event) {
        add("StockEvent", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.INVOICES, groupId = "timeline-invoices")
    @Transactional
    public void onInvoice(InvoiceEvent event) {
        add("InvoiceEvent", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.LEDGER, groupId = "timeline-ledger")
    @Transactional
    public void onLedger(LedgerEvent event) {
        add("LedgerEvent", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.FULFILLMENT, groupId = "timeline-fulfillment")
    @Transactional
    public void onPick(PickEvent event) {
        add("PickEvent", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.SHIPMENTS, groupId = "timeline-shipments")
    @Transactional
    public void onShip(ShipEvent event) {
        add("ShipEvent", event.orderId(), event.status());
    }

    @KafkaListener(topics = Channels.NOTICES, groupId = "timeline-notices")
    @Transactional
    public void onNotice(NoticeEvent event) {
        add("NoticeEvent", event.orderId(), event.status());
    }

    private void add(String kind, String orderId, String status) {
        String eventId = kind + ":" + orderId + ":" + status;
        if (entries.existsById(eventId)) {
            return;
        }
        TimelineEntry row = new TimelineEntry();
        row.setEventId(eventId);
        row.setOrderId(orderId);
        row.setKind(kind);
        row.setStatus(status);
        row.setRecordedAt(Instant.now());
        entries.save(row);
    }
}
