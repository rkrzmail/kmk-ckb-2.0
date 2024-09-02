package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustEmail(String email);

    Optional<Customer> findByCustEmailOrderByCustIdDesc(String email);

    Optional<Customer> findByCustCode(UUID custCode);

    Optional<Customer> findByCustEmailAndCustPin(String email, String pin);
}
