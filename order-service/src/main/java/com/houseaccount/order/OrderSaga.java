package com.houseaccount.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.CatalogLocated;
import com.houseaccount.common.Channels;
import com.houseaccount.common.CreditEvent;
import com.houseaccount.common.CreditReleaseCommand;
import com.houseaccount.common.CreditReserveCommand;
import com.houseaccount.common.HouseRules;
import com.houseaccount.common.InvoiceEvent;
import com.houseaccount.common.InvoiceIssueCommand;
import com.houseaccount.common.InvoiceVoidCommand;
import com.houseaccount.common.LedgerEvent;
import com.houseaccount.common.LedgerPostCommand;
import com.houseaccount.common.LedgerReverseCommand;
import com.houseaccount.common.NoticeCommand;
import com.houseaccount.common.NoticeEvent;
import com.houseaccount.common.OrderFact;
import com.houseaccount.common.PickCancelCommand;
import com.houseaccount.common.PickCommand;
import com.houseaccount.common.PickEvent;
import com.houseaccount.common.PriceCommand;
import com.houseaccount.common.PriceQuoted;
import com.houseaccount.common.ShipCommand;
import com.houseaccount.common.ShipEvent;
import com.houseaccount.common.StockEvent;
import com.houseaccount.common.StockReleaseCommand;
import com.houseaccount.common.StockReserveCommand;
import com.houseaccount.common.TaxAssessed;
import com.houseaccount.common.TaxCommand;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderSaga {

    private final EntityManager em;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;

    public OrderSaga(EntityManager em, OutboxRepository outbox, ObjectMapper mapper) {
        this.em = em;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @KafkaListener(topics = Channels.CATALOG, groupId = "order-catalog")
    @Transactional
    public void onCatalog(CatalogLocated event) {
        OrderEntity order = lock(event.orderId());
        if (!"DRAFT".equals(order.getStatus())) {
            return;
        }
        if (!"FOUND".equals(event.status())) {
            cancel(order, "UNKNOWN_SKU");
            return;
        }
        order.setWarehouseCode(event.warehouseCode());
        order.setStatus("LOCATED");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.PRICE, order.getId(),
                new PriceCommand(order.getId(), order.getSku(), order.getQuantity()));
    }

    @KafkaListener(topics = Channels.PRICES, groupId = "order-prices")
    @Transactional
    public void onPrice(PriceQuoted event) {
        OrderEntity order = lock(event.orderId());
        if (!"LOCATED".equals(order.getStatus())) {
            return;
        }
        if (!"QUOTED".equals(event.status())) {
            cancel(order, "PRICE");
            return;
        }
        order.setUnitPriceCents(event.unitPriceCents());
        order.setGoodsCents(event.goodsCents());
        order.setStatus("PRICED");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.TAX_ASSESS, order.getId(),
                new TaxCommand(order.getId(), order.getRegion(), order.getGoodsCents()));
    }

    @KafkaListener(topics = Channels.TAX, groupId = "order-tax")
    @Transactional
    public void onTax(TaxAssessed event) {
        OrderEntity order = lock(event.orderId());
        if (!"PRICED".equals(order.getStatus())) {
            return;
        }
        if (!"ASSESSED".equals(event.status())) {
            cancel(order, "TAX");
            return;
        }
        order.setTaxCents(event.taxCents());
        order.setTotalCents(event.totalCents());
        order.setStatus("TAXED");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.CREDIT_RESERVE, order.getId(),
                new CreditReserveCommand(order.getId(), order.getCustomerId(), order.getTotalCents()));
    }

    @KafkaListener(topics = Channels.CREDIT, groupId = "order-credit")
    @Transactional
    public void onCredit(CreditEvent event) {
        if (!"HELD".equals(event.status()) && !"DECLINED".equals(event.status())) {
            return;
        }
        OrderEntity order = lock(event.orderId());
        if (!"TAXED".equals(order.getStatus())) {
            return;
        }
        if (!"HELD".equals(event.status())) {
            cancel(order, "CREDIT");
            return;
        }
        order.setStatus("CREDIT_HELD");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.STOCK_RESERVE, order.getId(),
                new StockReserveCommand(order.getId(), order.getSku(), order.getQuantity()));
    }

    @KafkaListener(topics = Channels.STOCK, groupId = "order-stock")
    @Transactional
    public void onStock(StockEvent event) {
        if (!"RESERVED".equals(event.status()) && !"SHORT".equals(event.status()) && !"UNKNOWN".equals(event.status())) {
            return;
        }
        OrderEntity order = lock(event.orderId());
        if (!"CREDIT_HELD".equals(order.getStatus())) {
            return;
        }
        if (!"RESERVED".equals(event.status())) {
            cancel(order, "STOCK");
            return;
        }
        order.setStatus("ALLOCATED");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.INVOICE_ISSUE, order.getId(),
                new InvoiceIssueCommand(order.getId(), order.getCustomerId(), order.getTotalCents()));
    }

    @KafkaListener(topics = Channels.INVOICES, groupId = "order-invoices")
    @Transactional
    public void onInvoice(InvoiceEvent event) {
        if (!"ISSUED".equals(event.status())) {
            return;
        }
        OrderEntity order = lock(event.orderId());
        if (!"ALLOCATED".equals(order.getStatus())) {
            return;
        }
        order.setStatus("INVOICED");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.LEDGER_POST, order.getId(),
                new LedgerPostCommand(order.getId(), order.getCustomerId(), order.getTotalCents()));
    }

    @KafkaListener(topics = Channels.LEDGER, groupId = "order-ledger")
    @Transactional
    public void onLedger(LedgerEvent event) {
        if (!"POSTED".equals(event.status())) {
            return;
        }
        OrderEntity order = lock(event.orderId());
        if (!"INVOICED".equals(order.getStatus())) {
            return;
        }
        order.setStatus("POSTED");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.PICK, order.getId(),
                new PickCommand(order.getId(), order.getWarehouseCode()));
    }

    @KafkaListener(topics = Channels.FULFILLMENT, groupId = "order-fulfillment")
    @Transactional
    public void onPick(PickEvent event) {
        if (!"PICKED".equals(event.status()) && !"FAILED".equals(event.status())) {
            return;
        }
        OrderEntity order = lock(event.orderId());
        if (!"POSTED".equals(order.getStatus())) {
            return;
        }
        if (!"PICKED".equals(event.status())) {
            cancel(order, "PICK_FAILED");
            return;
        }
        order.setStatus("PICKED");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.SHIP, order.getId(),
                new ShipCommand(order.getId(), order.getShipToken()));
    }

    @KafkaListener(topics = Channels.SHIPMENTS, groupId = "order-shipments")
    @Transactional
    public void onShip(ShipEvent event) {
        if (!"BOOKED".equals(event.status()) && !"FAILED".equals(event.status())) {
            return;
        }
        OrderEntity order = lock(event.orderId());
        if (!"PICKED".equals(order.getStatus())) {
            return;
        }
        if (!"BOOKED".equals(event.status())) {
            cancel(order, "SHIPMENT_FAILED");
            return;
        }
        order.setStatus("SHIPPED");
        fact(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.NOTICE, order.getId(),
                new NoticeCommand(order.getId(), order.getEmail()));
    }

    @KafkaListener(topics = Channels.NOTICES, groupId = "order-notices")
    @Transactional
    public void onNotice(NoticeEvent event) {
        OrderEntity order = lock(event.orderId());
        if (!"SHIPPED".equals(order.getStatus()) || !"NONE".equals(order.getNoticeStatus())) {
            return;
        }
        order.setNoticeStatus(event.status());
    }

    private OrderEntity lock(String id) {
        OrderEntity order = em.find(OrderEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (order == null) {
            throw new IllegalStateException("order missing " + id);
        }
        return order;
    }

    private void fact(OrderEntity order) {
        OutboxWriter.kafka(outbox, mapper, Channels.ORDERS, order.getId(),
                new OrderFact(order.getId(), order.getStatus(), order.getReason() == null ? "" : order.getReason()));
    }

    private void cancel(OrderEntity order, String reason) {
        String status = order.getStatus();
        order.setStatus("CANCELLED");
        order.setReason(reason);
        fact(order);
        if (HouseRules.releaseCredit(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.CREDIT_RELEASE, order.getId(),
                    new CreditReleaseCommand(order.getId(), order.getCustomerId(), order.getTotalCents()));
        }
        if (HouseRules.releaseStock(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.STOCK_RELEASE, order.getId(),
                    new StockReleaseCommand(order.getId(), order.getSku(), order.getQuantity()));
        }
        if (HouseRules.voidInvoice(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.INVOICE_VOID, order.getId(), new InvoiceVoidCommand(order.getId()));
        }
        if (HouseRules.reverseLedger(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.LEDGER_REVERSE, order.getId(),
                    new LedgerReverseCommand(order.getId(), order.getCustomerId(), order.getTotalCents()));
        }
        if (HouseRules.cancelPick(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.PICK_CANCEL, order.getId(), new PickCancelCommand(order.getId()));
        }
    }
}
