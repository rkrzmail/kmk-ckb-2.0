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

    @Query("SELECT d FROM Debtor d WHERE d.debtorName = :debtorName AND d.signerStatus = 'active' ORDER BY d.dtmCrt DESC")
    List<Debtor> findActiveSignerByDebtorName(@Param("debtorName") String debtorName);

    @Query("SELECT d FROM Debtor d WHERE d.financingHdrCode = :financingHdrCode AND d.signerStatus = 'active' ORDER BY d.dtmCrt DESC")
    List<Debtor> findKaryawanByFinancingHdrCode(@Param("financingHdrCode") String financingHdrCode);
}
