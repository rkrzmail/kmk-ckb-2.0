package com.kmkbe.scheduled;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SchedulerLogRepository extends JpaRepository<SchedulerLog, Long> {
  Optional<SchedulerLog> findTopByJobNameAndStatusAndStartTimeAfterOrderByStartTimeDesc(
      String jobName, String status, LocalDateTime after
  );
}

