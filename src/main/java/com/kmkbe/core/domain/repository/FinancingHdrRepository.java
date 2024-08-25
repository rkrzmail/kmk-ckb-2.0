package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancingHdrRepository extends JpaRepository<FinancingHdr, UUID> {
    Optional<FinancingHdr> findByFinancingHdrCode(UUID code);

    Optional<FinancingHdr> findFirstByCustomerOrderByFinancingHdrIdDesc(Customer customer);

    Long countByCustomerAndFinancingStatus(Customer customer, String status);
}
