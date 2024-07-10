package com.kmkbe.modules.common.repository;

import com.kmkbe.modules.common.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
}
