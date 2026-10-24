package com.houseaccount.timeline;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimelineController {

    private final TimelineRepository entries;

    public TimelineController(TimelineRepository entries) {
        this.entries = entries;
    }

    @GetMapping("/timelines/{orderId}")
    public List<TimelineEntry> timeline(@PathVariable String orderId) {
        return entries.findByOrderIdOrderByRecordedAtAsc(orderId);
    }
}
