package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.loan_submission.entity.FinancingHdr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancingHdrRepository extends JpaRepository<FinancingHdr, UUID> {
    Optional<FinancingHdr> findByFinancingHdrCode(UUID code);

    Optional<FinancingHdr> findFirstByCustomerOrderByFinancingHdrIdDesc(Customer customer);
}
