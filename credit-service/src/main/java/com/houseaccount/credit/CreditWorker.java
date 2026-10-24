package com.houseaccount.credit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.CreditEvent;
import com.houseaccount.common.CreditReleaseCommand;
import com.houseaccount.common.CreditReserveCommand;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreditWorker {

    private final AccountRepository accounts;
    private final HoldRepository holds;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public CreditWorker(AccountRepository accounts, HoldRepository holds, OutboxRepository outbox,
            ObjectMapper mapper, EntityManager em) {
        this.accounts = accounts;
        this.holds = holds;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.CREDIT_RESERVE, concurrency = "4")
    @Transactional
    public void onReserve(CreditReserveCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":credit")) {
            return;
        }
        if (!accounts.existsById(command.customerId()) || accounts.reserve(command.customerId(), command.amountCents()) == 0) {
            OutboxWriter.kafka(outbox, mapper, Channels.CREDIT, command.orderId(), new CreditEvent(command.orderId(), "DECLINED"));
            return;
        }
        HoldEntity hold = new HoldEntity();
        hold.setOrderId(command.orderId());
        hold.setCustomerId(command.customerId());
        hold.setAmountCents(command.amountCents());
        holds.save(hold);
        OutboxWriter.kafka(outbox, mapper, Channels.CREDIT, command.orderId(), new CreditEvent(command.orderId(), "HELD"));
    }

    @RabbitListener(queues = Channels.CREDIT_RELEASE, concurrency = "4")
    @Transactional
    public void onRelease(CreditReleaseCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":credit-release")) {
            return;
        }
        int deleted = em.createNativeQuery("DELETE FROM credit_holds WHERE order_id = :id")
                .setParameter("id", command.orderId())
                .executeUpdate();
        if (deleted != 1 || accounts.release(command.customerId(), command.amountCents()) != 1) {
            throw new IllegalStateException("credit release missed " + command.orderId());
        }
        OutboxWriter.kafka(outbox, mapper, Channels.CREDIT, command.orderId(), new CreditEvent(command.orderId(), "RELEASED"));
    }
}
