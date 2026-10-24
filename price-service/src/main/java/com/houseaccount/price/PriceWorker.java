package com.houseaccount.price;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.PriceCommand;
import com.houseaccount.common.PriceQuoted;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PriceWorker {

    private final PriceRepository prices;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public PriceWorker(PriceRepository prices, OutboxRepository outbox, ObjectMapper mapper, EntityManager em) {
        this.prices = prices;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.PRICE, concurrency = "4")
    @Transactional
    public void onPrice(PriceCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":price")) {
            return;
        }
        PriceEntity price = prices.findById(command.sku()).orElse(null);
        if (price == null || command.quantity() <= 0) {
            OutboxWriter.kafka(outbox, mapper, Channels.PRICES, command.orderId(),
                    new PriceQuoted(command.orderId(), 0, 0, "MISSING"));
            return;
        }
        int goods;
        try {
            goods = Math.multiplyExact(price.getUnitPriceCents(), command.quantity());
        } catch (ArithmeticException ex) {
            OutboxWriter.kafka(outbox, mapper, Channels.PRICES, command.orderId(),
                    new PriceQuoted(command.orderId(), price.getUnitPriceCents(), 0, "REJECTED"));
            return;
        }
        OutboxWriter.kafka(outbox, mapper, Channels.PRICES, command.orderId(),
                new PriceQuoted(command.orderId(), price.getUnitPriceCents(), goods, "QUOTED"));
    }
}
