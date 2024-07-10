package com.kmkbe.modules.kredit.repository;

import com.kmkbe.modules.kredit.entity.FinancingHdr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancingHdrRepository extends JpaRepository<FinancingHdr, UUID> {
    Optional<FinancingHdr> findByFinancingHdrCode(UUID code);
}
