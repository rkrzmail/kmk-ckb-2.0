package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
}
