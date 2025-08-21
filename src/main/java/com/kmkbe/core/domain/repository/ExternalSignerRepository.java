package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.ExternalSigner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalSignerRepository extends JpaRepository<ExternalSigner, Long> {
}
