package com.houseaccount.shipment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class QueueConfig {

    @Bean
    public Queue locateDlq() {
        return QueueBuilder.durable(Channels.LOCATE_DLQ).build();
    }

    @Bean
    @DependsOn("locateDlq")
    public Queue locateQueue() {
        return QueueBuilder.durable(Channels.LOCATE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.LOCATE_DLQ)
                .build();
    }

    @Bean
    public Queue priceDlq() {
        return QueueBuilder.durable(Channels.PRICE_DLQ).build();
    }

    @Bean
    @DependsOn("priceDlq")
    public Queue priceQueue() {
        return QueueBuilder.durable(Channels.PRICE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.PRICE_DLQ)
                .build();
    }

    @Bean
    public Queue taxDlq() {
        return QueueBuilder.durable(Channels.TAX_DLQ).build();
    }

    @Bean
    @DependsOn("taxDlq")
    public Queue taxQueue() {
        return QueueBuilder.durable(Channels.TAX_ASSESS)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.TAX_DLQ)
                .build();
    }

    @Bean
    public Queue creditReserveDlq() {
        return QueueBuilder.durable(Channels.CREDIT_RESERVE_DLQ).build();
    }

    @Bean
    @DependsOn("creditReserveDlq")
    public Queue creditReserveQueue() {
        return QueueBuilder.durable(Channels.CREDIT_RESERVE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.CREDIT_RESERVE_DLQ)
                .build();
    }

    @Bean
    public Queue creditReleaseDlq() {
        return QueueBuilder.durable(Channels.CREDIT_RELEASE_DLQ).build();
    }

    @Bean
    @DependsOn("creditReleaseDlq")
    public Queue creditReleaseQueue() {
        return QueueBuilder.durable(Channels.CREDIT_RELEASE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.CREDIT_RELEASE_DLQ)
                .build();
    }

    @Bean
    public Queue stockReserveDlq() {
        return QueueBuilder.durable(Channels.STOCK_RESERVE_DLQ).build();
    }

    @Bean
    @DependsOn("stockReserveDlq")
    public Queue stockReserveQueue() {
        return QueueBuilder.durable(Channels.STOCK_RESERVE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.STOCK_RESERVE_DLQ)
                .build();
    }

    @Bean
    public Queue stockReleaseDlq() {
        return QueueBuilder.durable(Channels.STOCK_RELEASE_DLQ).build();
    }

    @Bean
    @DependsOn("stockReleaseDlq")
    public Queue stockReleaseQueue() {
        return QueueBuilder.durable(Channels.STOCK_RELEASE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.STOCK_RELEASE_DLQ)
                .build();
    }

    @Bean
    public Queue invoiceIssueDlq() {
        return QueueBuilder.durable(Channels.INVOICE_ISSUE_DLQ).build();
    }

    @Bean
    @DependsOn("invoiceIssueDlq")
    public Queue invoiceIssueQueue() {
        return QueueBuilder.durable(Channels.INVOICE_ISSUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.INVOICE_ISSUE_DLQ)
                .build();
    }

    @Bean
    public Queue invoiceVoidDlq() {
        return QueueBuilder.durable(Channels.INVOICE_VOID_DLQ).build();
    }

    @Bean
    @DependsOn("invoiceVoidDlq")
    public Queue invoiceVoidQueue() {
        return QueueBuilder.durable(Channels.INVOICE_VOID)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.INVOICE_VOID_DLQ)
                .build();
    }

    @Bean
    public Queue ledgerPostDlq() {
        return QueueBuilder.durable(Channels.LEDGER_POST_DLQ).build();
    }

    @Bean
    @DependsOn("ledgerPostDlq")
    public Queue ledgerPostQueue() {
        return QueueBuilder.durable(Channels.LEDGER_POST)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.LEDGER_POST_DLQ)
                .build();
    }

    @Bean
    public Queue ledgerReverseDlq() {
        return QueueBuilder.durable(Channels.LEDGER_REVERSE_DLQ).build();
    }

    @Bean
    @DependsOn("ledgerReverseDlq")
    public Queue ledgerReverseQueue() {
        return QueueBuilder.durable(Channels.LEDGER_REVERSE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.LEDGER_REVERSE_DLQ)
                .build();
    }

    @Bean
    public Queue pickDlq() {
        return QueueBuilder.durable(Channels.PICK_DLQ).build();
    }

    @Bean
    @DependsOn("pickDlq")
    public Queue pickQueue() {
        return QueueBuilder.durable(Channels.PICK)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.PICK_DLQ)
                .build();
    }

    @Bean
    public Queue pickCancelDlq() {
        return QueueBuilder.durable(Channels.PICK_CANCEL_DLQ).build();
    }

    @Bean
    @DependsOn("pickCancelDlq")
    public Queue pickCancelQueue() {
        return QueueBuilder.durable(Channels.PICK_CANCEL)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.PICK_CANCEL_DLQ)
                .build();
    }

    @Bean
    public Queue shipDlq() {
        return QueueBuilder.durable(Channels.SHIP_DLQ).build();
    }

    @Bean
    @DependsOn("shipDlq")
    public Queue shipQueue() {
        return QueueBuilder.durable(Channels.SHIP)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.SHIP_DLQ)
                .build();
    }

    @Bean
    public Queue noticeDlq() {
        return QueueBuilder.durable(Channels.NOTICE_DLQ).build();
    }

    @Bean
    @DependsOn("noticeDlq")
    public Queue noticeQueue() {
        return QueueBuilder.durable(Channels.NOTICE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.NOTICE_DLQ)
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
