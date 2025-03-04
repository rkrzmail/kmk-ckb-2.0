package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.PolicyAgreement;
import com.kmkbe.core.domain.entity.PolicyAgreementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyAgreementHistoryRepository extends JpaRepository<PolicyAgreementHistory, Long> {
    List<PolicyAgreementHistory> findByPolicyCode(String policyCode);
}

