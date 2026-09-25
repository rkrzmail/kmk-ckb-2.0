package com.kmkbe.core.domain.repository;

import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.Invoice;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

  Optional<Invoice> findFirstByCustomerAndBouwheerInvNoAndCustInvNo(
    Customer customer,
    String bouwheerInvNo,
    String custInvNo
  );

  Page<Invoice> findByCustomer(
    Customer cust,
    Specification<Invoice> spec,
    Pageable pageable
  );

  @Query(value = "select i.* from financing_hdr fh " +
    "join financing_dtl fd on fh.financing_hdr_code = fd.financing_hdr_code " +
    "join invoice i on fd.invoice_code = i.invoice_code " +
    "where cast(fh.cust_code as text) = :custCode " +
    "and (fh.financing_status = '' or fh.financing_status IS NULL) " +
    "and (fh.financing_step = '' or fh.financing_step IS NULL)",
    nativeQuery = true)
  List<Invoice> findInvoicesByCustCode(@Param("custCode") String custCode);
}
