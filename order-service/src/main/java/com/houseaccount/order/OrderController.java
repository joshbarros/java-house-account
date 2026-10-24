package com.houseaccount.order;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseaccount.common.CatalogLocateCommand;
import com.houseaccount.common.Channels;
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
public class OrderController {

    private final OrderRepository orders;
    private final DirectoryRepository directory;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;

    public OrderController(OrderRepository orders, DirectoryRepository directory, OutboxRepository outbox, ObjectMapper mapper) {
        this.orders = orders;
        this.directory = directory;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @PostMapping("/orders")
    @Transactional
    public ResponseEntity<OrderEntity> create(@RequestBody CreateOrder request) {
        if (request == null || blank(request.requestId()) || blank(request.customerId()) || blank(request.sku())
                || blank(request.region()) || blank(request.shipToken())
                || request.quantity() == null || request.quantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestId, customerId, sku, region, shipToken, and quantity are required");
        }
        OrderEntity existing = orders.findByRequestId(request.requestId().trim()).orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(existing);
        }
        CustomerDirectory customer = directory.findById(request.customerId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID().toString());
        order.setRequestId(request.requestId().trim());
        order.setCustomerId(customer.getCustomerId());
        order.setEmail(customer.getEmail());
        order.setSku(request.sku().trim());
        order.setQuantity(request.quantity());
        order.setRegion(request.region().trim());
        order.setShipToken(request.shipToken().trim());
        order.setStatus("DRAFT");
        order.setNoticeStatus("NONE");
        order.setReason("");
        orders.save(order);
        OutboxWriter.rabbit(outbox, mapper, Channels.LOCATE, order.getId(), new CatalogLocateCommand(order.getId(), order.getSku()));
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/orders/{id}")
    public OrderEntity order(@PathVariable String id) {
        return orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }

    @GetMapping("/directory/{customerId}")
    public CustomerDirectory directory(@PathVariable String customerId) {
        return directory.findById(customerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateOrder(String requestId, String customerId, String sku, Integer quantity, String region, String shipToken) {
    }
}
