package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.entity.CustomerCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerCompanyRepository extends JpaRepository<CustomerCompany, Long> {
    Optional<CustomerCompany> findByCustCode(UUID custCode);
}
