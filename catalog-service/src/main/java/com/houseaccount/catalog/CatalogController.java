package com.houseaccount.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CatalogController {

    private final SkuRepository skus;

    public CatalogController(SkuRepository skus) {
        this.skus = skus;
    }

    @PostMapping("/skus")
    public ResponseEntity<SkuEntity> create(@RequestBody CreateSku request) {
        if (request == null || blank(request.sku()) || blank(request.warehouseCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sku and warehouseCode are required");
        }
        String sku = request.sku().trim();
        if (skus.existsById(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "sku exists");
        }
        SkuEntity row = new SkuEntity();
        row.setSku(sku);
        row.setWarehouseCode(request.warehouseCode().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(skus.save(row));
    }

    @GetMapping("/skus/{sku}")
    public SkuEntity sku(@PathVariable String sku) {
        return skus.findById(sku).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "sku not found"));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateSku(String sku, String warehouseCode) {
    }
}
