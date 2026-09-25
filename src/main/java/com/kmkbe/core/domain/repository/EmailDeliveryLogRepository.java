package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.EmailDeliveryLog;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailDeliveryLogRepository extends JpaRepository<EmailDeliveryLog, Long> {
  List<EmailDeliveryLog> findByCustomerCodeOrderByRequestedAtDesc(UUID customerCode);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select d from EmailDeliveryLog d where d.emailDeliveryId = :id")
  Optional<EmailDeliveryLog> findByIdForUpdate(@Param("id") Long id);
}
