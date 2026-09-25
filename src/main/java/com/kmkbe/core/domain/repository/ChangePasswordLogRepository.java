package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.ChangePasswordLog;
import com.kmkbe.modules.customer.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChangePasswordLogRepository extends JpaRepository<ChangePasswordLog, Long> {
    Optional<List<ChangePasswordLog>> findAllByCustCode(Customer cust);
}
