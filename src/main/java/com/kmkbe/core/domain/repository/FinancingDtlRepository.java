package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancingDtlRepository extends JpaRepository<FinancingDtl, UUID>, JpaSpecificationExecutor<FinancingDtl> {
  Optional<List<FinancingDtl>> findAllByFinancingHdr(FinancingHdr financingHdr);

  List<FinancingDtl> findAllByFinancingHdrOrderByDtmCrtDesc(FinancingHdr financingHdr);

  @Query(
    value = """
      
      SELECT  fd.* FROM financing_dtl  fd
      JOIN financing_hdr fh ON (fd.financing_hdr_code=fh.financing_hdr_code)
      JOIN invoice i ON (i.invoice_code=fd.invoice_code)
      WHERE
      fh.cust_code  = :custCode and
      fh.financing_status is not null  and
      fh.financing_status <> 'LIVE' and
      fh.financing_status <> '' and
      i.invoice_due_date BETWEEN CURRENT_DATE and date_add(NOW(),INTERVAL '14 DAY')
      
      """,
    countQuery = """
      
      SELECT  COUNT(fd.*) FROM financing_dtl  fd
      JOIN financing_hdr fh ON (fd.financing_hdr_code=fh.financing_hdr_code)
      JOIN invoice i ON (i.invoice_code=fd.invoice_code)
      WHERE 
      fh.cust_code = :custCode and
      fh.financing_status is not null  and
      fh.financing_status <> 'LIVE' and
      fh.financing_status <> '' and
      i.invoice_due_date BETWEEN CURRENT_DATE and date_add(NOW(),INTERVAL '14 DAY')
      
      """,

    nativeQuery = true
  )
  Page<FinancingDtl> findByCustomer(
    @Param("custCode") String custCode,
    Pageable pageable
  );


  Page<FinancingDtl> findByFinancingHdr(
    FinancingHdr financingHdr,
    Pageable pageable
  );

  @NonNull
  Page<FinancingDtl> findAll(
    @NonNull Specification<FinancingDtl> spec,
    @NonNull Pageable pageable
  );

  List<FinancingDtl> findByBouwheerInvNoIn(List<String> invoiceNumbers);

}
