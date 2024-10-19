package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {
    Optional<Invoice> findByBouwheerInvNo(String bouwheerInvNo);

    Long countByCustomer(Customer customer);

    Optional<Invoice> findByCustomerAndBouwheerInvNoAndCustInvNo(
            Customer customer,
            String bouwheerInvNo,
            String custInvNo
    );

    Page<Invoice> findByCustomer(
            Customer cust,
            Specification<Invoice> spec,
            Pageable pageable
    );

    List<Invoice> findAllByCustomer(
            Customer customer
    );


}
