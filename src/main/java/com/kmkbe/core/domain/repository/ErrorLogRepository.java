package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
}
