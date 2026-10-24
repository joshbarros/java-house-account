package com.houseaccount.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.CatalogLocateCommand;
import com.houseaccount.common.CatalogLocated;
import com.houseaccount.common.Channels;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogWorker {

    private final SkuRepository skus;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public CatalogWorker(SkuRepository skus, OutboxRepository outbox, ObjectMapper mapper, EntityManager em) {
        this.skus = skus;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.LOCATE)
    @Transactional
    public void onLocate(CatalogLocateCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":locate")) {
            return;
        }
        SkuEntity sku = skus.findById(command.sku()).orElse(null);
        if (sku == null) {
            OutboxWriter.kafka(outbox, mapper, Channels.CATALOG, command.orderId(),
                    new CatalogLocated(command.orderId(), command.sku(), null, "MISSING"));
            return;
        }
        OutboxWriter.kafka(outbox, mapper, Channels.CATALOG, command.orderId(),
                new CatalogLocated(command.orderId(), command.sku(), sku.getWarehouseCode(), "FOUND"));
    }
}
