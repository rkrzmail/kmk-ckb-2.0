package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.entity.ChangePasswordLog;
import com.kmkbe.modules.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChangePasswordLogRepository extends JpaRepository<ChangePasswordLog, Long> {
    Optional<List<ChangePasswordLog>> findAllByCustCode(Customer cust);
}
