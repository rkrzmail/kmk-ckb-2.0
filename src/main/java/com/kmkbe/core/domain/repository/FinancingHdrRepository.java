package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.dto.ProyeksiReportDto;
import com.kmkbe.core.domain.dto.SummaryByAODto;
import com.kmkbe.core.domain.dto.SummaryByBranchDto;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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


    Page<FinancingHdr> findAllByCustomer( Customer customer,
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

    @Query("SELECT f.disburseAmt, f.financingAmt, c.custName, b.bouwheerName, cwr.plafondAmt, p.retentionAmt, f.mstBranch.branchCode " +
            "FROM FinancingHdr f " +
            "JOIN Customer c ON f.customer.custCode = c.custCode " +
            "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
            "JOIN Cwr cwr ON c.custCode = cwr.customer.custCode " +
            "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
            "JOIN PaymentReceiveHistory p ON p.agreementCode = a.agreementCode")
    Page<Object[]> findFinancingDataByFinancingHdrCode(Pageable pageable);

    @Query("SELECT distinct " +
            "c.custName, " +
            "c.custIdNo, " +
            "c.existingCust, " +
            "b.bouwheerName, " +
            "f.mstBranch.branchCode, " +
            "cwr.cwrCode, " +
            "a.agreementCode, " +
            "(SELECT COUNT(*) FROM Cwr c WHERE LENGTH(c.cwrCode) > 1), " +
            "f.retention, " +
            "cwr.plafondAmt, " +
            "f.financingAmt, " +
            "cwr.plafondAmt - cwr.realisationAmt, " +
            "f.adminFeeAmt, " +
            "f.othersFeeAmt, " +
            "f.financingAmt, " +
            "f.disburseDate, " +
            "f.financingStatus, " +
            "i.invoiceDueDate, " +
            "c.dtmUpd, " +
            "f.financingDate, " +
            "ac.goliveDate " +
            "FROM FinancingHdr f " +
            "JOIN Customer c ON f.customer.custCode = c.custCode " +
            "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
            "JOIN Cwr cwr ON cwr.customer.custCode = c.custCode " +
            "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
            "JOIN Agreement ac ON ac.agreementCode = a.agreementCode " +
            "JOIN Invoice i ON i.customer.custCode = c.custCode " +
            "WHERE DATE(f.financingDate) BETWEEN :startDate AND :endDate " )
//            "WHERE c.isActive = TRUE")
    Page<Object[]> findSummaryByCustCode(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);

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

}
