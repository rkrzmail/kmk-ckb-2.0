package com.kmkbe.core.domain.repository;


import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.entity.PaymentReceiveHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentReceiveHistoryRepository extends JpaRepository<PaymentReceiveHistory, UUID>, JpaSpecificationExecutor<Invoice> {


    Optional<PaymentReceiveHistory> findTopByAgreementCode(String agreementCode);

}
