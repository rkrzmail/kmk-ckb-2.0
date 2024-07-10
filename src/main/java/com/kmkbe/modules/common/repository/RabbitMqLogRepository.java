package com.kmkbe.modules.common.repository;

import com.kmkbe.modules.common.entity.RabbitmqLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RabbitMqLogRepository extends JpaRepository<RabbitmqLog, Long> {
}
