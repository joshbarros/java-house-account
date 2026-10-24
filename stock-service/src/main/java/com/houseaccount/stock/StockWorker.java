package com.houseaccount.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.StockEvent;
import com.houseaccount.common.StockReleaseCommand;
import com.houseaccount.common.StockReserveCommand;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockWorker {

    private final ItemRepository items;
    private final AllocationRepository allocations;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public StockWorker(ItemRepository items, AllocationRepository allocations, OutboxRepository outbox,
            ObjectMapper mapper, EntityManager em) {
        this.items = items;
        this.allocations = allocations;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.STOCK_RESERVE, concurrency = "4")
    @Transactional
    public void onReserve(StockReserveCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":stock")) {
            return;
        }
        if (!items.existsById(command.sku())) {
            OutboxWriter.kafka(outbox, mapper, Channels.STOCK, command.orderId(), new StockEvent(command.orderId(), "UNKNOWN"));
            return;
        }
        if (items.reserve(command.sku(), command.quantity()) != 1) {
            OutboxWriter.kafka(outbox, mapper, Channels.STOCK, command.orderId(), new StockEvent(command.orderId(), "SHORT"));
            return;
        }
        AllocationEntity allocation = new AllocationEntity();
        allocation.setOrderId(command.orderId());
        allocation.setSku(command.sku());
        allocation.setQuantity(command.quantity());
        allocations.save(allocation);
        OutboxWriter.kafka(outbox, mapper, Channels.STOCK, command.orderId(), new StockEvent(command.orderId(), "RESERVED"));
    }

    @RabbitListener(queues = Channels.STOCK_RELEASE, concurrency = "4")
    @Transactional
    public void onRelease(StockReleaseCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":stock-release")) {
            return;
        }
        int deleted = em.createNativeQuery("DELETE FROM allocations WHERE order_id = :id")
                .setParameter("id", command.orderId())
                .executeUpdate();
        if (deleted != 1 || items.release(command.sku(), command.quantity()) != 1) {
            throw new IllegalStateException("stock release missed " + command.orderId());
        }
        OutboxWriter.kafka(outbox, mapper, Channels.STOCK, command.orderId(), new StockEvent(command.orderId(), "RELEASED"));
    }
}
