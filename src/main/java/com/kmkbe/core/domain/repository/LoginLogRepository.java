package com.kmkbe.core.domain.repository;

import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    Optional<LoginLog> findByCustCode(Customer cust);

    Optional<LoginLog> findTopByCustCode(Customer cust);
}
