package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobLogRepository extends JpaRepository<JobLog, Long> {
}
