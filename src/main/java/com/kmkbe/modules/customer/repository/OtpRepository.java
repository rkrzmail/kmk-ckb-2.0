package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.entity.OtpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpLog, Long> {
    Optional<OtpLog> findTopByEmailAndOtpCodeOrderByDtmCrtDesc(String email, String code);

    OtpLog findTopByEmail(String email);

    OtpLog findByEmail(String email);

    @Query(
            value = "select count(*) from otp_log where email = ?1 and date(dtm_crt) = current_date",
            nativeQuery = true
    )
    Long countTodayRequestByEmail(String email);
}
