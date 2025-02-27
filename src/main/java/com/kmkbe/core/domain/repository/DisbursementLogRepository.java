package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.DisbursementLog;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DisbursementLogRepository extends JpaRepository<DisbursementLog, String>, JpaSpecificationExecutor<DisbursementLog> {
    DisbursementLog findByAgreement_AgreementCode(String agreementCode);



    List<DisbursementLog> findAllByAgreement(@NotNull(message = "Agreement cannot be null") Agreement agreement);

    void deleteAllByAgreement(@NotNull(message = "Agreement cannot be null") Agreement agreement);
}

