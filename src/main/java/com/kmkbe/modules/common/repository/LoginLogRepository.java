package com.kmkbe.modules.common.repository;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.common.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    Optional<LoginLog> findByCustCode(Customer cust);

    Optional<LoginLog> findTopByCustCode(Customer cust);
}
