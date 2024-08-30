package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AgreementRepository extends JpaRepository<Agreement, String>, JpaSpecificationExecutor<Agreement> {
    Optional<Agreement> findTopByAgreementCodeOrderByAgreementId(String agreementCode);
}
