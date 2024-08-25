package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancingDtlRepository extends JpaRepository<FinancingDtl, UUID> {
    Optional<List<FinancingDtl>> findAllByFinancingHdr(FinancingHdr financingHdr);

    Optional<FinancingDtl> findFirstByBouwheerInvNo(String bouwheerInvNo);

    Page<FinancingDtl> findByFinancingHdr(
            FinancingHdr financingHdr,
            Pageable pageable
    );
}
