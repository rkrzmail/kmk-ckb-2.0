package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.entity.OtpLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpLog, Long> {
    Optional<OtpLog> findByEmailAndOtpCode(String email, String code);

    OtpLog findByEmail(String email);
}
