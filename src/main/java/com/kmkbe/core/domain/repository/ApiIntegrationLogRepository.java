package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiIntegrationLogRepository extends JpaRepository<ApiIntegrationLog, Long> {
}
