package com.kmkbe.modules.common.repository;

import com.kmkbe.modules.common.entity.ApiIntegrationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiIntegrationLogRepository extends JpaRepository<ApiIntegrationLog, Long> {
}
