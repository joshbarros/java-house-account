package com.houseaccount.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.InvoiceEvent;
import com.houseaccount.common.InvoiceIssueCommand;
import com.houseaccount.common.InvoiceVoidCommand;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InvoiceWorker {

    private final InvoiceRepository invoices;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public InvoiceWorker(InvoiceRepository invoices, OutboxRepository outbox, ObjectMapper mapper, EntityManager em) {
        this.invoices = invoices;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.INVOICE_ISSUE)
    @Transactional
    public void onIssue(InvoiceIssueCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":invoice")) {
            return;
        }
        InvoiceEntity row = new InvoiceEntity();
        row.setOrderId(command.orderId());
        row.setCustomerId(command.customerId());
        row.setTotalCents(command.totalCents());
        row.setStatus("ISSUED");
        invoices.save(row);
        OutboxWriter.kafka(outbox, mapper, Channels.INVOICES, command.orderId(), new InvoiceEvent(command.orderId(), "ISSUED"));
    }

    @RabbitListener(queues = Channels.INVOICE_VOID)
    @Transactional
    public void onVoid(InvoiceVoidCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":invoice-void")) {
            return;
        }
        int updated = em.createNativeQuery("UPDATE invoices SET status = 'VOID' WHERE order_id = :id AND status = 'ISSUED'")
                .setParameter("id", command.orderId())
                .executeUpdate();
        if (updated != 1) {
            throw new IllegalStateException("invoice void missed " + command.orderId());
        }
        OutboxWriter.kafka(outbox, mapper, Channels.INVOICES, command.orderId(), new InvoiceEvent(command.orderId(), "VOID"));
    }
}
