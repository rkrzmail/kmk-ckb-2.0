package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.AgreementFileSigning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgreementFileSigningRepository extends JpaRepository<AgreementFileSigning, Long> {

  Optional<AgreementFileSigning> findByDocumentId(String documentId);

  Optional<AgreementFileSigning> findByDocumentIdAndEmailSigner(String documentId, String emailSigner);

  List<AgreementFileSigning> findByAgreementCode(String agreementCode);

  @Query("SELECT a FROM AgreementFileSigning a WHERE a.signer = :signer AND a.fileTypeCode='E_SIGN_DOC'")
  List<AgreementFileSigning> findByKaryawan(@Param("signer") String signerName);

  long countBySigner(String signerName);

  @Query(value = """
    SELECT COUNT(DISTINCT afs.agreement_code)
    FROM agreement_file_signing afs
    JOIN financing_hdr agreement_fh
      ON CAST(agreement_fh.financing_hdr_code AS VARCHAR) = afs.financing_hdr_code
    WHERE agreement_fh.cust_code = (
      SELECT current_fh.cust_code
      FROM financing_hdr current_fh
      WHERE current_fh.financing_hdr_code = :financingHdrCode
    )
    """, nativeQuery = true)
  long countUploadedAgreementsByCustomer(
    @Param("financingHdrCode") UUID financingHdrCode
  );

  @Query(value = """
    SELECT COUNT(DISTINCT afs.agreement_code)
    FROM agreement_file_signing afs
    JOIN financing_hdr agreement_fh
      ON CAST(agreement_fh.financing_hdr_code AS VARCHAR) = afs.financing_hdr_code
    WHERE agreement_fh.cust_code = (
      SELECT current_fh.cust_code
      FROM financing_hdr current_fh
      WHERE current_fh.financing_hdr_code = :financingHdrCode
    )
    AND UPPER(agreement_fh.financing_step) IN ('SIGNING', 'SIGNED', 'PAID')
    """, nativeQuery = true)
  long countRunningUploadedAgreementsByCustomer(
    @Param("financingHdrCode") UUID financingHdrCode
  );

  @Query(value = """
    SELECT COUNT(DISTINCT afs.agreement_code)
    FROM agreement_file_signing afs
    JOIN financing_hdr agreement_fh
      ON CAST(agreement_fh.financing_hdr_code AS VARCHAR) = afs.financing_hdr_code
    WHERE agreement_fh.cust_code = (
      SELECT current_fh.cust_code
      FROM financing_hdr current_fh
      WHERE current_fh.financing_hdr_code = :financingHdrCode
    )
    AND UPPER(agreement_fh.financing_step) = 'COMPLETED'
    """, nativeQuery = true)
  long countCompletedUploadedAgreementsByCustomer(
    @Param("financingHdrCode") UUID financingHdrCode
  );

  Optional<AgreementFileSigning> findFirstByAgreementCode(String agreementCode);
}
