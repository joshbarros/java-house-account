package com.houseaccount.tax;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.HouseRules;
import com.houseaccount.common.TaxAssessed;
import com.houseaccount.common.TaxCommand;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TaxWorker {

    private final AssessmentRepository assessments;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public TaxWorker(AssessmentRepository assessments, OutboxRepository outbox, ObjectMapper mapper, EntityManager em) {
        this.assessments = assessments;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.TAX_ASSESS)
    @Transactional
    public void onTax(TaxCommand command) {
        if (!InboxClaims.claim(em, command.orderId() + ":tax")) {
            return;
        }
        AssessmentEntity row = new AssessmentEntity();
        row.setOrderId(command.orderId());
        if (HouseRules.blocked(command.region())) {
            row.setTaxCents(0);
            row.setTotalCents(0);
            row.setStatus("REJECTED");
            assessments.save(row);
            OutboxWriter.kafka(outbox, mapper, Channels.TAX, command.orderId(),
                    new TaxAssessed(command.orderId(), 0, 0, "REJECTED"));
            return;
        }
        int taxCents = HouseRules.exempt(command.region()) ? 0 : HouseRules.taxOf(command.goodsCents());
        int total = HouseRules.total(command.goodsCents(), taxCents);
        row.setTaxCents(taxCents);
        row.setTotalCents(total);
        row.setStatus("ASSESSED");
        assessments.save(row);
        OutboxWriter.kafka(outbox, mapper, Channels.TAX, command.orderId(),
                new TaxAssessed(command.orderId(), taxCents, total, "ASSESSED"));
    }
}
