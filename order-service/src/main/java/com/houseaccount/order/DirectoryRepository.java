package com.houseaccount.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectoryRepository extends JpaRepository<CustomerDirectory, String> {
}
