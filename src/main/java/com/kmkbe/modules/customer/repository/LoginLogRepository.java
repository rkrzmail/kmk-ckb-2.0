package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
}
