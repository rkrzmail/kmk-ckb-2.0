package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementRepository extends JpaRepository<Agreement, String> {
}
