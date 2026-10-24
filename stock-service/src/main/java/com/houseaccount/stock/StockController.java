package com.houseaccount.stock;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StockController {

    private final ItemRepository items;

    public StockController(ItemRepository items) {
        this.items = items;
    }

    @PostMapping("/items")
    public ResponseEntity<ItemEntity> create(@RequestBody CreateItem request) {
        if (request == null || request.sku() == null || request.sku().isBlank() || request.onHand() == null || request.onHand() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sku and onHand are required");
        }
        String sku = request.sku().trim();
        if (items.existsById(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "item exists");
        }
        ItemEntity row = new ItemEntity();
        row.setSku(sku);
        row.setOnHand(request.onHand());
        return ResponseEntity.status(HttpStatus.CREATED).body(items.save(row));
    }

    @GetMapping("/items/{sku}")
    public ItemEntity item(@PathVariable String sku) {
        return items.findById(sku).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));
    }

    public record CreateItem(String sku, Integer onHand) {
    }
}
