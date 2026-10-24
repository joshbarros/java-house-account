package com.houseaccount.invoice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class InvoiceController {

    private final InvoiceRepository invoices;

    public InvoiceController(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    @GetMapping("/invoices/{orderId}")
    public InvoiceEntity invoice(@PathVariable String orderId) {
        return invoices.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invoice not found"));
    }
}
