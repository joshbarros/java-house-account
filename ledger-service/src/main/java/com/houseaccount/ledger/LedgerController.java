package com.houseaccount.ledger;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LedgerController {

    private final ReceivableRepository receivables;

    public LedgerController(ReceivableRepository receivables) {
        this.receivables = receivables;
    }

    @GetMapping("/receivables/{customerId}")
    public ReceivableEntity receivable(@PathVariable String customerId) {
        return receivables.findById(customerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "receivable not found"));
    }
}
