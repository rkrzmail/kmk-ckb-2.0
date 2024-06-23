package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.entity.OtpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpLog, Long> {
    Optional<OtpLog> findByEmailAndOtpCode(String email, String code);

    OtpLog findByEmail(String email);
}
