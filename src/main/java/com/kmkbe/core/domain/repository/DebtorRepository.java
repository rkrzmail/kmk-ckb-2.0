package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Debtor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DebtorRepository extends JpaRepository<Debtor, Long> {
    boolean existsByIdentityNo(String identityNo);

    Optional<Debtor> findByEmail(String email);

    List<Debtor> findByFinancingHdrCode(String financingHdrCode);

    @Query("SELECT d FROM Debtor d WHERE d.financingHdrCode = :code ORDER BY d.dtmCrt DESC")
    List<Debtor> findByFinancingHdrCodeOrderByDtmCrtDesc(@Param("code") String code);

    @Query(value = """
    SELECT 
        d.karyawan_name AS karyawan_name,
        d.jabatan AS jabatan
    FROM debtors d
    WHERE d.financing_hdr_code = :financingHdrCode
        AND d.signer_status = 'active'
    """, nativeQuery = true)
    Optional<Map<String, Object>> findKaryawanByFinancingHdrCode(@Param("financingHdrCode") String financingHdrCode);

    @Query("SELECT d FROM Debtor d " +
            "WHERE d.financingHdrCode = :financingHdrCode " +
            "AND d.signerStatus = 'active'")
    Optional<Debtor> findActiveSignerByFinancingHdrCode(@Param("financingHdrCode") String financingHdrCode);

}
