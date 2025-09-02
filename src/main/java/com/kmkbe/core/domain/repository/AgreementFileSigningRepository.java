package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Debtor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgreementFileSigningRepository extends JpaRepository<AgreementFileSigning, Long> {

    Optional<AgreementFileSigning> findByDocumentId(String documentId);

    Optional<AgreementFileSigning> findByDocumentIdAndEmailSigner(String documentId, String emailSigner);

    List<AgreementFileSigning> findByAgreementCode(String agreementCode);

    @Query("SELECT a FROM AgreementFileSigning a WHERE a.financingHdrCode = :financingHdrCode")
    List<AgreementFileSigning> findByFinancing(@Param("financingHdrCode") String financingHdrCode);

    @Query("SELECT a FROM AgreementFileSigning a WHERE a.signer = :signer")
    List<AgreementFileSigning> findByKaryawan(@Param("signer") String signerName);

    @Query("SELECT a.documentId FROM AgreementFileSigning a WHERE a.agreementCode = :agreementCode")
    String findDocumentIdByAgreementCode(@Param("agreementCode") String agreementCode);

    long countByFinancingHdrCode(String financingHdrCode);

    long countByFinancingHdrCodeAndStamp(String financingHdrCode, String stamp);
}
