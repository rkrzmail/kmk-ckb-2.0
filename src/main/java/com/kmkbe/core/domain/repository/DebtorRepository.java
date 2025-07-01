package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Debtor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DebtorRepository extends JpaRepository<Debtor, Long> {

    List<Debtor> findByFinancingHdrCode(String financingHdrCode);
}
