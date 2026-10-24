package com.houseaccount.party;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.Channels;
import com.houseaccount.common.CustomerRegistered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CustomerController {

    private final CustomerRepository customers;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;

    public CustomerController(CustomerRepository customers, OutboxRepository outbox, ObjectMapper mapper) {
        this.customers = customers;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @PostMapping("/customers")
    @Transactional
    public ResponseEntity<CustomerEntity> create(@RequestBody CreateCustomer request) {
        if (request == null || blank(request.name()) || blank(request.email())
                || request.creditLimitCents() == null || request.creditLimitCents() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name, email, and creditLimitCents are required");
        }
        CustomerEntity customer = new CustomerEntity();
        customer.setId(UUID.randomUUID().toString());
        customer.setName(request.name().trim());
        customer.setEmail(request.email().trim());
        customer.setCreditLimitCents(request.creditLimitCents());
        customers.save(customer);
        OutboxWriter.kafka(outbox, mapper, Channels.CUSTOMERS, customer.getId(),
                new CustomerRegistered(customer.getId(), customer.getName(), customer.getEmail(), customer.getCreditLimitCents()));
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @GetMapping("/customers/{id}")
    public CustomerEntity customer(@PathVariable String id) {
        return customers.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateCustomer(String name, String email, Integer creditLimitCents) {
    }
}
