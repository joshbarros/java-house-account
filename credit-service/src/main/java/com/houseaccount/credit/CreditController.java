package com.houseaccount.credit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
public class CreditController {

    private final AccountRepository accounts;

    public CreditController(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/accounts/{id}")
    public AccountEntity account(@PathVariable String id) {
        return accounts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "account not found"));
    }
}
