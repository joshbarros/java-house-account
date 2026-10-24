package com.houseaccount.ledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.LedgerEvent;
import com.houseaccount.common.LedgerPostCommand;
import com.houseaccount.common.LedgerReverseCommand;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LedgerWorker {

    private final ReceivableRepository receivables;
    private final PostingRepository postings;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public LedgerWorker(ReceivableRepository receivables, PostingRepository postings, OutboxRepository outbox,
            ObjectMapper mapper, EntityManager em) {
        this.receivables = receivables;
        this.postings = postings;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.LEDGER_POST)
    @Transactional
    public void onPost(LedgerPostCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":ledger")) {
            return;
        }
        em.createNativeQuery("INSERT INTO receivables (customer_id, balance_cents) VALUES (:id, 0) ON CONFLICT DO NOTHING")
                .setParameter("id", command.customerId())
                .executeUpdate();
        if (receivables.debit(command.customerId(), command.amountCents()) != 1) {
            throw new IllegalStateException("receivable debit missed " + command.orderId());
        }
        PostingEntity posting = new PostingEntity();
        posting.setId(command.orderId());
        posting.setOrderId(command.orderId());
        posting.setCustomerId(command.customerId());
        posting.setAmountCents(command.amountCents());
        posting.setKind("POST");
        postings.save(posting);
        OutboxWriter.kafka(outbox, mapper, Channels.LEDGER, command.orderId(), new LedgerEvent(command.orderId(), "POSTED"));
    }

    @RabbitListener(queues = Channels.LEDGER_REVERSE)
    @Transactional
    public void onReverse(LedgerReverseCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":ledger-reverse")) {
            return;
        }
        if (!postings.existsById(command.orderId())) {
            throw new IllegalStateException("original posting missing " + command.orderId());
        }
        if (receivables.credit(command.customerId(), command.amountCents()) != 1) {
            throw new IllegalStateException("receivable credit missed " + command.orderId());
        }
        PostingEntity reversal = new PostingEntity();
        reversal.setId(command.orderId() + ":R");
        reversal.setOrderId(command.orderId());
        reversal.setCustomerId(command.customerId());
        reversal.setAmountCents(command.amountCents());
        reversal.setKind("REVERSAL");
        postings.save(reversal);
        OutboxWriter.kafka(outbox, mapper, Channels.LEDGER, command.orderId(), new LedgerEvent(command.orderId(), "REVERSED"));
    }
}
