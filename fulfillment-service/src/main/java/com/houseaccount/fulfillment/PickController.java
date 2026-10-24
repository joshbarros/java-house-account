package com.houseaccount.fulfillment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PickController {

    private final PickRepository picks;

    public PickController(PickRepository picks) {
        this.picks = picks;
    }

    @GetMapping("/picks/{orderId}")
    public PickEntity pick(@PathVariable String orderId) {
        return picks.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pick not found"));
    }
}
