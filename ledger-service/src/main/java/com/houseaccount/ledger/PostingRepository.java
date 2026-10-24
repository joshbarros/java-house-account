package com.houseaccount.ledger;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingRepository extends JpaRepository<PostingEntity, String> {
}
