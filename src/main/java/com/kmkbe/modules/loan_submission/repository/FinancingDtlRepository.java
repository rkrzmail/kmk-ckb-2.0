package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.loan_submission.entity.FinancingDtl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FinancingDtlRepository extends JpaRepository<FinancingDtl, UUID> {
}
