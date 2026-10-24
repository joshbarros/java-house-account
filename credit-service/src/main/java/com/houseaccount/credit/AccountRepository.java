package com.houseaccount.credit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE credit_accounts SET exposed_cents = exposed_cents + :amount WHERE id = :id AND exposed_cents + :amount <= credit_limit_cents", nativeQuery = true)
    int reserve(@Param("id") String id, @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE credit_accounts SET exposed_cents = exposed_cents - :amount WHERE id = :id AND exposed_cents >= :amount", nativeQuery = true)
    int release(@Param("id") String id, @Param("amount") int amount);
}
