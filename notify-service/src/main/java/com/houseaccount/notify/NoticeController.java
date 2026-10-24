package com.houseaccount.notify;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class NoticeController {

    private final NoticeRepository notices;

    public NoticeController(NoticeRepository notices) {
        this.notices = notices;
    }

    @GetMapping("/notices/{orderId}")
    public NoticeEntity notice(@PathVariable String orderId) {
        return notices.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notice not found"));
    }
}
