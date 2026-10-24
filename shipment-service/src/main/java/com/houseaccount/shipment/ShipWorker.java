package com.houseaccount.shipment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.HouseRules;
import com.houseaccount.common.ShipCommand;
import com.houseaccount.common.ShipEvent;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ShipWorker {

    private final ShipmentRepository shipments;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;
    private final TransactionTemplate tx;

    public ShipWorker(ShipmentRepository shipments, OutboxRepository outbox, ObjectMapper mapper, EntityManager em,
            PlatformTransactionManager manager) {
        this.shipments = shipments;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    @RabbitListener(queues = Channels.SHIP, concurrency = "4")
    public void onShip(ShipCommand command) {
        Boolean dead = tx.execute(status -> {
            if (!InboxClaims.claim(em, command.orderId() + ":ship")) {
                return false;
            }
            boolean declined = HouseRules.shipDeclined(command.shipToken());
            if (!declined) {
                ShipmentEntity row = new ShipmentEntity();
                row.setOrderId(command.orderId());
                row.setStatus("BOOKED");
                shipments.save(row);
            }
            OutboxWriter.kafka(outbox, mapper, Channels.SHIPMENTS, command.orderId(),
                    new ShipEvent(command.orderId(), declined ? "FAILED" : "BOOKED"));
            return declined;
        });
        if (Boolean.TRUE.equals(dead)) {
            throw new AmqpRejectAndDontRequeueException("carrier declined");
        }
    }
}
