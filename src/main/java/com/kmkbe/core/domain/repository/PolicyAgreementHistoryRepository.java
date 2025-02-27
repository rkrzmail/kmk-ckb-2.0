package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.PolicyAgreement;
import com.kmkbe.core.domain.entity.PolicyAgreementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyAgreementHistoryRepository extends JpaRepository<PolicyAgreementHistory, Long> {
}

