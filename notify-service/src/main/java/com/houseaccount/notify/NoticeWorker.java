package com.houseaccount.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.HouseRules;
import com.houseaccount.common.NoticeCommand;
import com.houseaccount.common.NoticeEvent;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class NoticeWorker {

    private final NoticeRepository notices;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;
    private final TransactionTemplate tx;

    public NoticeWorker(NoticeRepository notices, OutboxRepository outbox, ObjectMapper mapper, EntityManager em,
            PlatformTransactionManager manager) {
        this.notices = notices;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    @RabbitListener(queues = Channels.NOTICE, concurrency = "4")
    public void onNotice(NoticeCommand command) {
        Boolean dead = tx.execute(status -> {
            if (!InboxClaims.claim(em, command.orderId() + ":notice")) {
                return false;
            }
            boolean bounce = HouseRules.bounce(command.email());
            if (!bounce) {
                NoticeEntity row = new NoticeEntity();
                row.setOrderId(command.orderId());
                row.setEmail(command.email());
                row.setStatus("DELIVERED");
                notices.save(row);
            }
            OutboxWriter.kafka(outbox, mapper, Channels.NOTICES, command.orderId(),
                    new NoticeEvent(command.orderId(), bounce ? "BOUNCED" : "DELIVERED"));
            return bounce;
        });
        if (Boolean.TRUE.equals(dead)) {
            throw new AmqpRejectAndDontRequeueException("undeliverable address");
        }
    }
}
