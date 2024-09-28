package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.DisbursementLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DisbursementLogRepository extends JpaRepository<DisbursementLog, String>, JpaSpecificationExecutor<DisbursementLog> {
    DisbursementLog findByAgreement_AgreementCode(String agreementCode);
}
