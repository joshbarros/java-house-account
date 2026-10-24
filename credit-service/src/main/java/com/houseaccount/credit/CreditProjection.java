package com.houseaccount.credit;

import com.houseaccount.common.Channels;
import com.houseaccount.common.CustomerRegistered;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreditProjection {

    private final AccountRepository accounts;

    public CreditProjection(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @KafkaListener(topics = Channels.CUSTOMERS, groupId = "credit-customers")
    @Transactional
    public void onCustomer(CustomerRegistered event) {
        if (accounts.existsById(event.customerId())) {
            return;
        }
        AccountEntity account = new AccountEntity();
        account.setId(event.customerId());
        account.setCreditLimitCents(event.creditLimitCents());
        account.setExposedCents(0);
        accounts.save(account);
    }
}
