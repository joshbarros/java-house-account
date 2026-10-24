package com.houseaccount.timeline;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimelineRepository extends JpaRepository<TimelineEntry, String> {
    List<TimelineEntry> findByOrderIdOrderByRecordedAtAsc(String orderId);
}
