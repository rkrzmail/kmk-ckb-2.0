package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerPersonalRepository extends JpaRepository<CustomerPersonal, Long> {
    Optional<CustomerPersonal> findByCustCode(Customer cust);
}
