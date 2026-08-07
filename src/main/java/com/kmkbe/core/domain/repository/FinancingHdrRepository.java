package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.dto.ProyeksiReportDto;
import com.kmkbe.core.domain.dto.SummaryByBranchDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.modules.user.entity.MstBranch;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface FinancingHdrRepository extends JpaRepository<FinancingHdr, UUID>, JpaSpecificationExecutor<FinancingHdr> {
  Optional<FinancingHdr> findByFinancingHdrCode(UUID code);

  Optional<FinancingHdr> findFirstByCustomerOrderByFinancingHdrIdDesc(Customer customer);


  List<FinancingHdr> findAllByCustomer(@NotNull Customer customer);

  List<FinancingHdr> findAllByCustomerOrderByDtmCrtDesc(@NotNull Customer customer);

  Long countByCustomerAndFinancingStatus(Customer customer, String status);


  Page<FinancingHdr> findByOrderByFinancingHdrIdDesc(
    Pageable pageable
  );

  Page<FinancingHdr> findByFinancingStatusOrderByFinancingHdrIdDesc(
    String financingStatus,
    Pageable pageable
  );

  Page<FinancingHdr> findByFinancingStatusAndFinancingStepAndMstBranchOrderByFinancingHdrIdDesc(
    String financingStatus,
    String financingStepStatus,
    MstBranch mstBranch,
    Pageable pageable
  );


  Page<FinancingHdr> findByMstBranchOrderByFinancingHdrIdDesc(
    MstBranch mstBranch,
    Pageable pageable
  );

    @Query(
      value = """
        select 
            row_number () over (
                                order by
                                    fh.financing_hdr_id
                                )::integer as no, 
            fh.*
        from
            public.financing_hdr fh
            join public.customer c on fh.cust_code = c.cust_code
            join bouwheer bw on fh.bouwheer_code = bw.bouwheer_code
        where
            fh.branch_code = :branchCode
            and (TRUE = :#{#financingStatus == null} or fh.financing_status = :financingStatus)
            and (TRUE = :#{#custName == null} or c.cust_name like '%' || :custName || '%')
            and (TRUE = :#{#bouwheerName == null} or bw.bouwheer_name like '%' || :bouwheerName || '%')
        order by
            fh.dtm_crt desc
        """,
      countQuery = """
        select count(*)
        from
            public.financing_hdr fh
            join public.customer c on fh.cust_code = c.cust_code
            join bouwheer bw on fh.bouwheer_code = bw.bouwheer_code
        where
            fh.branch_code = :branchCode
            and (TRUE = :#{#financingStatus == null} or fh.financing_status = :financingStatus)
            and (TRUE = :#{#custName == null} or c.cust_name like '%' || :custName || '%')
            and (TRUE = :#{#bouwheerName == null} or bw.bouwheer_name like '%' || :bouwheerName || '%')
        """,
      nativeQuery = true
    )
    Page<FinancingHdr> findAllAssignmentFinancingRaw(
      @Param("branchCode") String branchCode,
      @Param("financingStatus") String financingStatus,
      @Param("custName") String custName,
      @Param("bouwheerName") String bouwheerName,
      Pageable pageable
    );

  @Query(
    value = """
      select
          fh.*
      from
          public.financing_hdr fh
      where
          fh.financing_status is not null and
          fh.financing_step is not null and
          fh.financing_hdr_code in (
                                       select
                                           fd.financing_hdr_code
                                       from
                                           public.financing_dtl fd
                                               join public.invoice iv on fd.invoice_code = iv.invoice_code
                                   )
          and nullif(fh.financing_status, '') is not null
          and nullif(fh.financing_step, '') is not null
      order by
          fh.dtm_crt desc
      """,
    countQuery = """
      select count(*)
      from
          financing_hdr fh
      where
          fh.financing_status is not null and
          fh.financing_step is not null and
          fh.financing_hdr_code in (
                                       select
                                           fd.financing_hdr_code
                                       from
                                           public.financing_dtl fd
                                               join public.invoice iv on fd.invoice_code = iv.invoice_code
                                   )
          and nullif(fh.financing_status, '') is not null
          and nullif(fh.financing_step, '') is not null
      """,
    nativeQuery = true
  )
  Page<FinancingHdr> findAllByRawOrder(
    Pageable pageable
  );


  @Query(
    value = """
      select
          fh.*
      from
          public.financing_hdr fh
      where
          fh.financing_status is not null and
          fh.financing_step is not null and
          fh.financing_hdr_code in (
                                       select
                                           fd.financing_hdr_code
                                       from
                                           public.financing_dtl fd
                                               join public.invoice iv on fd.invoice_code = iv.invoice_code
                                   )
          and nullif(fh.financing_status, '') is not null
          and nullif(fh.financing_step, '') is not null
      order by
          fh.dtm_crt desc
      """,
    countQuery = """
      select count(*)
      from
          financing_hdr fh
      where
          fh.financing_status is not null and
          fh.financing_step is not null and
          fh.financing_hdr_code in (
                                       select
                                           fd.financing_hdr_code
                                       from
                                           public.financing_dtl fd
                                               join public.invoice iv on fd.invoice_code = iv.invoice_code
                                   )
          and nullif(fh.financing_status, '') is not null
          and nullif(fh.financing_step, '') is not null
      """,
    nativeQuery = true
  )
  List<FinancingHdr> findAllByRaw(

  );


  @Query(
    value = """
      select
          fh.*
      from
          public.financing_hdr fh
      where
            fh.cust_code = :custCode and
          fh.financing_status is not null and
          fh.financing_step is not null and
          fh.financing_hdr_code in (
                                       select
                                           fd.financing_hdr_code
                                       from
                                           public.financing_dtl fd
                                               join public.invoice iv on fd.invoice_code = iv.invoice_code
                                   )
          and nullif(fh.financing_status, '') is not null
          and nullif(fh.financing_step, '') is not null
      order by
          fh.dtm_crt desc
      """,
    countQuery = """
      select count(*)
      from
          financing_hdr fh
      where
            fh.cust_code = :custCode and
          fh.financing_status is not null and
          fh.financing_step is not null and
          fh.financing_hdr_code in (
                                       select
                                           fd.financing_hdr_code
                                       from
                                           public.financing_dtl fd
                                               join public.invoice iv on fd.invoice_code = iv.invoice_code
                                   )
          and nullif(fh.financing_status, '') is not null
          and nullif(fh.financing_step, '') is not null
      """,
    nativeQuery = true
  )
  Page<FinancingHdr> findAllByRawOrder(
    @Param("custCode") String custCode,
    Pageable pageable
  );


  Page<FinancingHdr> findAllByCustomer(Customer customer,
                                       Pageable pageable
  );


  @Query(
    value = """
      select
          fh.*
      from
          public.financing_hdr fh
      where
          fh.cust_code = :custCode and
          fh.financing_status = :financingStatus and
          fh.financing_hdr_code in (
                                       select
                                           fd.financing_hdr_code
                                       from
                                           public.financing_dtl fd
                                               join public.invoice iv on fd.invoice_code = iv.invoice_code
                                   )
          and nullif(fh.financing_status, '') is not null
          and nullif(fh.financing_step, '') is not null
      order by
          fh.financing_hdr_id desc
      """,
    countQuery = """
      select count(*)
      from
          financing_hdr fh
      where
          fh.cust_code = :custCode and
          fh.financing_status = :financingStatus and
          fh.financing_hdr_code in (
                                       select
                                           fd.financing_hdr_code
                                       from
                                           public.financing_dtl fd
                                               join public.invoice iv on fd.invoice_code = iv.invoice_code
                                   )
          and nullif(fh.financing_status, '') is not null
          and nullif(fh.financing_step, '') is not null
      """,
    nativeQuery = true
  )
  Page<FinancingHdr> findAllForInvoice(
    @Param("custCode") String custCode,
    @Param("financingStatus") String financingStatus,
    Pageable pageable
  );

  @NonNull
  Page<FinancingHdr> findAll(
    @NonNull Specification<FinancingHdr> spec,
    @NonNull Pageable pageable
  );


  List<FinancingHdr> findAllByFinancingStatusAndFinancingStep(@NotNull String financingStatus, @NotNull String financingStep);

  // Native query untuk mendapatkan branch_code berdasarkan financing_hdr_code
  @Query(value = "SELECT branch_code FROM financing_hdr WHERE financing_hdr_code = :financingHdrCode", nativeQuery = true)
  String findBranchCodeByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode);

  @Query("SELECT f FROM FinancingHdr f WHERE f.customer.custCode = :custCode ORDER BY f.financingDate DESC")
  List<FinancingHdr> findLatestFinancingHdrByCustCode(@Param("custCode") UUID custCode, Pageable pageable);

  @Query("SELECT DISTINCT f.disburseAmt, f.financingAmt, c.custName, b.bouwheerName, cwr.plafondAmt, p.retentionAmt, f.mstBranch.branchCode " +
    "FROM FinancingHdr f " +
    "JOIN Customer c ON f.customer.custCode = c.custCode " +
    "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
    "JOIN Cwr cwr ON c.custCode = cwr.customer.custCode " +
    "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
    "JOIN PaymentReceiveHistory p ON p.agreementCode = a.agreementCode " +
    "WHERE DATE(f.financingDate) BETWEEN :startDate AND :endDate")
  Page<Object[]> findFinancingDataByFinancingHdrCode(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);

//    @Query("SELECT distinct " +
//            "c.custName, " +
//            "c.custIdNo, " +
//            "c.existingCust, " +
//            "b.bouwheerName, " +
//            "f.mstBranch.branchCode, " +
//            "cwr.cwrCode, " +
//            "a.agreementCode, " +
//            "(SELECT COUNT(*) FROM Cwr c WHERE LENGTH(c.cwrCode) > 1)," +
//            "f.retention, " +
//            "cwr.plafondAmt, " +
//            "f.financingAmt, " +
//            "cwr.plafondAmt - cwr.realisationAmt, " +
//            "f.adminFeeAmt, " +
//            "f.othersFeeAmt, " +
//            "f.disburseDate, " +
//            "f.financingStatus, " +
//            "i.invoiceDueDate, " +
//            "c.dtmUpd, " +
//            "f.financingDate, " +
//            "ac.goliveDate " +
//            "FROM FinancingHdr f " +
//            "JOIN Customer c ON f.customer.custCode = c.custCode " +
//            "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
//            "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
//            "JOIN Cwr cwr ON cwr.cwrCode = a.cwr.cwrCode " +
//            "JOIN Agreement ac ON ac.agreementCode = a.agreementCode " +
//            "JOIN Invoice i ON i.customer.custCode = c.custCode " +
//            "WHERE DATE(f.financingDate) BETWEEN :startDate AND :endDate")

  /// /            "WHERE c.isActive = TRUE")
//    Page<Object[]> findSummaryByCustCode(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);
  @Query("SELECT NEW com.kmkbe.core.domain.dto.ProyeksiReportDto(" +
    "c.custName, " +
    "c.existingCust, " +
    "b.bouwheerName, " +
    "i.custInvNo, " +
    "i.invoiceAmt, " +
    "f.financingAmt, " +
    "i.invoiceDueDate, " +
    "f.financingDate) " +
    "FROM FinancingHdr f " +
    "LEFT JOIN Customer c ON f.customer.custCode = c.custCode " +
    "LEFT JOIN FinancingDtl fd ON f.financingHdrCode = fd.financingHdr.financingHdrCode " +
    "LEFT JOIN Invoice i ON fd.invoice.invoiceCode = i.invoiceCode " +
    "LEFT JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
    "WHERE DATE(i.invoiceDueDate) BETWEEN :startDate AND :endDate")
  Page<ProyeksiReportDto> findActiveCustomersWithInvoiceDetails(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);

  @Query("SELECT new com.kmkbe.core.domain.dto.SummaryByBranchDto(" +
    "c.custName, " +
    "f.mstBranch.branchCode, " +
    "c.custIdNo, " +
    "b.bouwheerName, " +
    "f.disburseAmt, " +
    "cwr.plafondAmt, " +
    "f.financingAmt, " +
    "p.retentionAmt) " +
    "FROM FinancingHdr f " +
    "JOIN Customer c ON f.customer.custCode = c.custCode " +
    "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
    "JOIN Cwr cwr ON f.customer.custCode = cwr.customer.custCode " +
    "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
    "JOIN PaymentReceiveHistory p ON p.agreementCode = a.agreementCode " +
    "WHERE DATE(f.financingDate) BETWEEN :startDate AND :endDate AND c.custIdTypeCode = 'NPWP'")
  Page<SummaryByBranchDto> findSummaryBranch(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);

  @Query("SELECT " +
    "c.custName, " +
    "c.custIdNo, " +
    "b.bouwheerName, " +
    "f.mstBranch.branchCode, " +
    "a.agreementCode, " +
    "ph.goliveDate, " +
//            "(SELECT COUNT(*) FROM Cwr c WHERE LENGTH(c.cwrCode) > 1), " +
    "(SELECT COUNT(DISTINCT a1.agreementCode) " +
    "FROM Agreement a1 WHERE a1.financingHdr.financingHdrCode = f.financingHdrCode) AS agreementCodeCount, " +
    "f.financingAmt, " +
    "(ph.totalInvAmt - ph.interestAmt), " +
    "f.effectiveRate, " +
    "ph.retentionAmt, " +
    "ph.lcAmt, " +
    "ph.dueDate, " +
    "ph.settlementDte, " +
    "f.financingStatus " +
    "FROM FinancingHdr f " +
    "JOIN Customer c ON f.customer.custCode = c.custCode " +
    "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
    "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
    "JOIN PaymentReceiveHistory ph ON ph.agreementCode = a.agreementCode " +
    "WHERE DATE(ph.dueDate) BETWEEN :startDate AND :endDate")
//            "WHERE c.isActive = TRUE")
  Page<Object[]> findDueDateReport(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);

  @Query("SELECT " +
    "c.custName, " +
    "c.custIdNo, " +
    "c.existingCust, " +
    "b.bouwheerName, " +
    "f.mstBranch.branchCode, " +
    "cwr.cwrCode, " +
    "a.agreementCode, " +
    "COUNT(DISTINCT a.agreementCode) AS agreementCodeCount, " +  // Menghitung jumlah agreement_code per cwr_code
    "100 - f.retention, " +
    "cwr.plafondAmt, " +
    "f.financingAmt, " +
    "cwr.plafondAmt - cwr.realisationAmt AS sisaPlafond, " +
    "f.adminFeeAmt, " +
    "f.othersFeeAmt, " +
    "f.disburseDate, " +
    "f.financingStatus, " +
    "i.invoiceDueDate, " +
    "c.dtmUpd, " +
    "f.financingDate, " +
    "ac.goliveDate " +  // Di sini hanya join Agreement untuk goliveDate
    "FROM FinancingHdr f " +
    "JOIN Customer c ON f.customer.custCode = c.custCode " +
    "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
    "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
    "JOIN Cwr cwr ON cwr.cwrCode = a.cwr.cwrCode " +
    "JOIN Invoice i ON i.customer.custCode = c.custCode " +
    "LEFT JOIN Agreement ac ON ac.agreementCode = a.agreementCode " +  // Diubah jadi LEFT JOIN untuk menghindari data yang hilang
    "WHERE DATE(f.financingDate) BETWEEN :startDate AND :endDate " +
    "AND a.status = 'Live' " +  // Pastikan hanya mengambil customer yang aktif
    "GROUP BY " +
    "c.custName, " +
    "c.custIdNo, " +
    "c.existingCust, " +
    "b.bouwheerName, " +
    "f.mstBranch.branchCode, " +
    "cwr.cwrCode, " +
    "a.agreementCode, " +
    "f.retention, " +
    "cwr.plafondAmt, " +
    "cwr.realisationAmt, " +
    "f.financingAmt, " +
    "f.adminFeeAmt, " +
    "f.othersFeeAmt, " +
    "f.disburseDate, " +
    "f.financingStatus, " +
    "i.invoiceDueDate, " +
    "c.dtmUpd, " +
    "f.financingDate, " +
    "ac.goliveDate")
  Page<Object[]> findSummaryByCustCode(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);

  @Query(
    value = "SELECT c.cust_name " +
      "FROM financing_hdr f " +
      "JOIN customer c ON f.cust_code = c.cust_code " +
      "WHERE f.financing_hdr_code = :financingHdrCode",
    nativeQuery = true)
  String findDebtorNameByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode);

  @Query(
    value = "SELECT d.karyawan_name " +
      "FROM financing_hdr f " +
      "JOIN customer c ON f.cust_code = c.cust_code " +
      "JOIN debtors d ON c.cust_name = d.debtor_name " +
      "WHERE f.financing_hdr_code = :financingHdrCode " +
      "AND d.signhub_status = 'active' ",
    nativeQuery = true)
  List<String> findSignerNameByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode);

  @Query(value = """
    SELECT COUNT(*) 
    FROM financing_hdr fh
    WHERE fh.cust_code = (
        SELECT f.cust_code 
        FROM financing_hdr f 
        WHERE f.financing_hdr_code = :financingHdrCode
    )
    AND fh.financing_step IN ('SIGNING', 'SIGNED', 'PAID')
    """, nativeQuery = true)
  Long countSigningAndSigned(@Param("financingHdrCode") String financingHdrCode);

  @Query(value = """
    SELECT COUNT(*) 
    FROM financing_hdr fh
    WHERE fh.cust_code = (
        SELECT f.cust_code 
        FROM financing_hdr f 
        WHERE f.financing_hdr_code = :financingHdrCode
    )
    AND fh.financing_step = 'COMPLETED'
    """, nativeQuery = true)
  Long countCompleted(@Param("financingHdrCode") String financingHdrCode);


  @Query(value = """
    select i.* from financing_hdr fh
    join financing_dtl fd on fh.financing_hdr_code = fd.financing_hdr_code
    join invoice i on fd.invoice_code = i.invoice_code
    where fh.vendor_id = :vendorId 
    and (fh.financing_status = '' or fh.financing_status IS NULL) 
    and (fh.financing_step = '' or fh.financing_step IS NULL)
    """, nativeQuery = true)
  List<Invoice> findFinancingHeaderByVendorId(@Param("vendorId") String vendorId);


}
