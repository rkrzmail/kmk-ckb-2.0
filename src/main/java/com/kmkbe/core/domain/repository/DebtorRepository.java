package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Debtor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DebtorRepository extends JpaRepository<Debtor, Long> {


    Optional<Debtor> findByEmail(String email);

    List<Debtor> findByFinancingHdrCode(String financingHdrCode);


    @Query("SELECT d FROM Debtor d WHERE d.financingHdrCode = :financingHdrCode ORDER BY d.debtorId DESC")
    Optional<Debtor> findTopByFinancingHdrCodeOrderByIdDesc(@Param("financingHdrCode") String financingHdrCode);
}
