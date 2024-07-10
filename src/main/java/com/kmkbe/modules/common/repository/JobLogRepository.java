package com.kmkbe.modules.common.repository;

import com.kmkbe.modules.common.entity.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobLogRepository extends JpaRepository<JobLog, Long> {
}
