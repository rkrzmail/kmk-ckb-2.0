package com.kmkbe.modules.kredit.repository;

import com.kmkbe.modules.kredit.entity.FinancingDtl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FinancingDtlRepository extends JpaRepository<FinancingDtl, UUID> {
}
