package com.houseaccount.fulfillment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.HouseRules;
import com.houseaccount.common.PickCancelCommand;
import com.houseaccount.common.PickCommand;
import com.houseaccount.common.PickEvent;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class PickWorker {

    private final PickRepository picks;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;
    private final TransactionTemplate tx;

    public PickWorker(PickRepository picks, OutboxRepository outbox, ObjectMapper mapper, EntityManager em,
            PlatformTransactionManager manager) {
        this.picks = picks;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    @RabbitListener(queues = Channels.PICK, concurrency = "4")
    public void onPick(PickCommand command) {
        Boolean dead = tx.execute(status -> {
            if (!InboxClaims.claim(em, command.orderId() + ":pick")) {
                return false;
            }
            boolean closed = HouseRules.closed(command.warehouseCode());
            if (!closed) {
                PickEntity row = new PickEntity();
                row.setOrderId(command.orderId());
                row.setWarehouseCode(command.warehouseCode());
                row.setStatus("PICKED");
                picks.save(row);
            }
            OutboxWriter.kafka(outbox, mapper, Channels.FULFILLMENT, command.orderId(),
                    new PickEvent(command.orderId(), closed ? "FAILED" : "PICKED"));
            return closed;
        });
        if (Boolean.TRUE.equals(dead)) {
            throw new AmqpRejectAndDontRequeueException("warehouse closed");
        }
    }

    @RabbitListener(queues = Channels.PICK_CANCEL)
    public void onCancel(PickCancelCommand command) {
        tx.executeWithoutResult(status -> {
            if (!InboxClaims.claim(em, command.orderId() + ":pick-cancel")) {
                return;
            }
            int updated = em.createNativeQuery("UPDATE picks SET status = 'CANCELLED' WHERE order_id = :id AND status = 'PICKED'")
                    .setParameter("id", command.orderId())
                    .executeUpdate();
            if (updated != 1) {
                throw new IllegalStateException("pick cancel missed " + command.orderId());
            }
            OutboxWriter.kafka(outbox, mapper, Channels.FULFILLMENT, command.orderId(),
                    new PickEvent(command.orderId(), "CANCELLED"));
        });
    }
}
