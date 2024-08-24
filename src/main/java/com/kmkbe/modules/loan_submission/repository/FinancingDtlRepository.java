package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.loan_submission.entity.FinancingDtl;
import com.kmkbe.modules.loan_submission.entity.FinancingHdr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancingDtlRepository extends JpaRepository<FinancingDtl, UUID> {
    Optional<List<FinancingDtl>> findAllByFinancingHdr(FinancingHdr financingHdr);

    Optional<FinancingDtl> findFirstByBouwheerInvNo(String bouwheerInvNo);
}
