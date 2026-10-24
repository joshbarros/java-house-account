package com.houseaccount.price;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PriceController {

    private final PriceRepository prices;

    public PriceController(PriceRepository prices) {
        this.prices = prices;
    }

    @PostMapping("/prices")
    public ResponseEntity<PriceEntity> create(@RequestBody CreatePrice request) {
        if (request == null || request.sku() == null || request.sku().isBlank()
                || request.unitPriceCents() == null || request.unitPriceCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sku and a positive unitPriceCents are required");
        }
        String sku = request.sku().trim();
        if (prices.existsById(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "price exists");
        }
        PriceEntity row = new PriceEntity();
        row.setSku(sku);
        row.setUnitPriceCents(request.unitPriceCents());
        return ResponseEntity.status(HttpStatus.CREATED).body(prices.save(row));
    }

    public record CreatePrice(String sku, Integer unitPriceCents) {
    }
}
