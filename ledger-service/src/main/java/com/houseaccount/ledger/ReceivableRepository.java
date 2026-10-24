package com.houseaccount.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceivableRepository extends JpaRepository<ReceivableEntity, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE receivables SET balance_cents = balance_cents + :amount WHERE customer_id = :id", nativeQuery = true)
    int debit(@Param("id") String id, @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE receivables SET balance_cents = balance_cents - :amount WHERE customer_id = :id AND balance_cents >= :amount", nativeQuery = true)
    int credit(@Param("id") String id, @Param("amount") int amount);
}
