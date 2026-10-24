package com.houseaccount.shipment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ShipmentController {

    private final ShipmentRepository shipments;

    public ShipmentController(ShipmentRepository shipments) {
        this.shipments = shipments;
    }

    @GetMapping("/shipments/{orderId}")
    public ShipmentEntity shipment(@PathVariable String orderId) {
        return shipments.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "shipment not found"));
    }
}
