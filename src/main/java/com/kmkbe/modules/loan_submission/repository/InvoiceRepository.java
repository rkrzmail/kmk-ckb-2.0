package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.loan_submission.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {
    Optional<Invoice> findByBouwheerInvNo(String bouwheerInvNo);

    Long countByCustCode(Customer customer);

    Optional<Invoice> findByCustCodeAndBouwheerInvNoAndCustInvNo(
            Customer customer,
            String bouwheerInvNo,
            String custInvNo
    );

    Page<Invoice> findByCustCode(
            Customer cust,
            Specification<Invoice> spec,
            Pageable pageable
    );
}
