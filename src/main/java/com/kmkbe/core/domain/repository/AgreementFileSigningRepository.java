package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgreementFileSigningRepository extends JpaRepository<AgreementFileSigning, Long> {

    @Query("SELECT a FROM AgreementFileSigning a WHERE a.agreementCode IN :agreementCodes")
    List<AgreementFileSigning> findByAgreementCodes(@Param("agreementCodes") List<String> agreementCodes);

    @Query("SELECT a.documentId FROM AgreementFileSigning a WHERE a.agreementCode = :agreementCode")
    String findDocumentIdByAgreementCode(@Param("agreementCode") String agreementCode);
}
