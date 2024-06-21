package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.entity.CustomerPersonal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerPersonalRepository extends JpaRepository<CustomerPersonal, Long> {
    Optional<CustomerPersonal> findByCustCode(UUID custCode);
}
