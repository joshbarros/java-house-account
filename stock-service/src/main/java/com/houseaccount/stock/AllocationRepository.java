package com.houseaccount.stock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AllocationRepository extends JpaRepository<AllocationEntity, String> {
}
