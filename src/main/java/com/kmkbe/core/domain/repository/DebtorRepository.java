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

    List<Debtor> findByDebtorName(String debtorName);

    @Query("SELECT d FROM Debtor d WHERE d.financingHdrCode = :code ORDER BY d.dtmCrt DESC")
    List<Debtor> findByFinancingHdrCodeOrderByDtmCrtDesc(@Param("code") String code);

    @Query(value = """
    SELECT 
        d.karyawan_name AS karyawan_name,
        d.jabatan AS jabatan,
        d.identity_no,
        d.alamat
    FROM debtors d
    WHERE d.debtor_name = :debtorName
        AND d.signer_status = 'active'
    ORDER BY d.dtm_crt DESC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Map<String, Object>> findKaryawanByDebtorName(@Param("debtorName") String debtorName);

    @Query("SELECT d FROM Debtor d " +
            "WHERE d.debtorName = :debtorName " +
            "AND d.signerStatus = 'active'")
    Optional<Debtor> findActiveSignerByDebtorName(@Param("debtorName") String debtorName);

}
