package com.kmkbe.scheduled;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerLogRepository extends JpaRepository<SchedulerLog, Long> {
}

