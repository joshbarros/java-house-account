package com.houseaccount.order;

import com.houseaccount.common.Channels;
import com.houseaccount.common.CustomerRegistered;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DirectoryProjection {

    private final DirectoryRepository directory;

    public DirectoryProjection(DirectoryRepository directory) {
        this.directory = directory;
    }

    @KafkaListener(topics = Channels.CUSTOMERS, groupId = "order-customers")
    @Transactional
    public void onCustomer(CustomerRegistered event) {
        if (directory.existsById(event.customerId())) {
            return;
        }
        CustomerDirectory row = new CustomerDirectory();
        row.setCustomerId(event.customerId());
        row.setEmail(event.email());
        directory.save(row);
    }
}
