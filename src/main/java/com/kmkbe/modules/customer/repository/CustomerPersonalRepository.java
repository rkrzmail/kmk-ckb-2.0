package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.CustomerPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerPersonalRepository extends JpaRepository<CustomerPersonal, Long> {
    Optional<CustomerPersonal> findByCustCode(Customer cust);
}
