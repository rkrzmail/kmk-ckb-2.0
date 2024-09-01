package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AgreementFileRepository extends JpaRepository<AgreementFile, Long>, JpaSpecificationExecutor<AgreementFile> {
    Optional<AgreementFile> findByAgreement(Agreement agreement);

    Optional<AgreementFile> findTopByAgreementOrderByAgreementFileId(Agreement agreement);
}
