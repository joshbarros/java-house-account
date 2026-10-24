package com.houseaccount.fulfillment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PickRepository extends JpaRepository<PickEntity, String> {
}
