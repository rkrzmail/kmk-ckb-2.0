package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.RabbitmqLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RabbitMqLogRepository extends JpaRepository<RabbitmqLog, Long> {
}
