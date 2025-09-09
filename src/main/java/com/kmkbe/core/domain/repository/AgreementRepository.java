package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.FinancingHdr;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface AgreementRepository extends JpaRepository<Agreement, String>, JpaSpecificationExecutor<Agreement> {
    Optional<Agreement> findTopByAgreementCode(String agreementCode);

    List<Agreement> findAllByStatus(@Size(max = 20) @NotNull(message = "Status cannot be null") String status);

    Optional<Agreement> findTopByAgreementCodeOrderByAgreementId(String agreementCode);

    @Query(
            value = """
                    select
                        ag.*
                    from
                        public.agreement ag
                            join public.agreement_file agf on ag.agreement_code = agf.agreement_code
                    
                    """,
            nativeQuery = true
    )
    List<Agreement> viewApprovalStatusNoPending();

    @Query(
            value = """
                    select
                        ag.*
                    from
                        public.agreement ag
                            join public.agreement_file agf on ag.agreement_code = agf.agreement_code
                    where
                         ag.approval_flag is null
                      OR ag.approval_flag in ('', 'false');
                    """,
            nativeQuery = true
    )
    List<Agreement> viewApprovalStatusPending();

    @Query(
            value = "SELECT cwr.cwr_code, ag.agreement_code, fh.financing_hdr_code, " +
                    "bw.bouwheer_code, bw.bouwheer_name, ag.financing_amt, " +
                    "fh.disburse_date as disburse_date, ag.currency, fh.disburse_amt, " +
                    "ct.cust_name, ct.cust_code," +
                    "ROW_NUMBER() OVER (ORDER BY fh.disburse_date DESC) as no " +
                    "FROM public.agreement ag " +
                    "JOIN public.cwr ON ag.cwr_code = cwr.cwr_code " +
                    "JOIN public.financing_hdr fh ON ag.financing_hdr_code = fh.financing_hdr_code " +
                    "JOIN public.bouwheer bw ON fh.bouwheer_code = bw.bouwheer_code " +
                    "JOIN public.customer ct ON cwr.cust_code = ct.cust_code " +
                    "WHERE ag.cwr_code = :cwrCode AND ag.financing_hdr_code = :financingHdrCode " +
                    "ORDER BY fh.disburse_date DESC",
            countQuery = "SELECT COUNT(*) FROM public.agreement ag " +
                    "JOIN public.cwr ON ag.cwr_code = cwr.cwr_code " +
                    "JOIN public.financing_hdr fh ON ag.financing_hdr_code = fh.financing_hdr_code " +
                    "JOIN public.bouwheer bw ON fh.bouwheer_code = bw.bouwheer_code " +
                    "WHERE ag.cwr_code = :cwrCode AND ag.financing_hdr_code = :financingHdrCode",
            nativeQuery = true
    )
    Page<Map<String, Object>> findAllListByCwrAndFinancingRaw(
            @Param("cwrCode") String cwrCode,
            @Param("financingHdrCode") String financingHdrCode,
            Pageable pageable
    );

    List<Agreement> findAllByCwr(@NotNull(message = "Cwr cannot be null") Cwr cwr);

    Optional<Agreement> findTopByFinancingHdr(FinancingHdr financingHdr);
    List<Agreement> findByFinancingHdr_FinancingHdrCode(UUID financinghdrCode);

    @Query("SELECT a FROM Agreement a " +
            "JOIN FETCH a.cwr " +
            "JOIN FETCH a.cwr.customer " +
            "JOIN FETCH a.financingHdr " +
            "WHERE a.financingHdr.financingHdrCode = :financingHdrCode AND a.agreementCode = :agreementNo")
    Optional<Agreement> findByFinancingHdr_FinancingHdrCode2(@Param("financingHdrCode") UUID financingHdrCode, String agreementNo);

    @Query("SELECT a FROM Agreement a WHERE a.financingHdr.financingHdrCode = :financingHdrCode")
    Optional<Agreement> findAgreement(@Param("financingHdrCode") UUID financingHdrCode);


    @Query("SELECT a FROM Agreement a WHERE a.financingHdr.financingHdrCode = :financingHdrCode AND a.agreementCode = :agreementCode")
    Optional<Agreement> findByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

    Optional<Agreement> findByAgreementCode(String agreementCode);

    @Query("SELECT a.agreementCode FROM Agreement a WHERE a.financingHdr.financingHdrCode = :financingHdrCode AND a.agreementCode = :agreementCode")
    Optional<String> findAgreementCodeByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

    @Query("SELECT a.cwr.cwrCode FROM Agreement a WHERE a.financingHdr.financingHdrCode = :financingHdrCode AND a.agreementCode = :agreementCode")
    Optional<String> findCwrCodeByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

    @Query("""
        SELECT cu.custName
        FROM Agreement a
        JOIN Cwr c ON a.cwr.cwrCode = c.cwrCode
        JOIN Customer cu ON c.customer.custCode = cu.custCode
        WHERE a.financingHdr.financingHdrCode = :financingHdrCode AND a.agreementCode = :agreementCode
    """)
    Optional<String> findCustNameByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

    @Query(value = """
        SELECT 
            w.cwr_code,
            w.cwr_start_date
        FROM agreement a
        JOIN cwr w ON a.cwr_code = w.cwr_code
        WHERE a.financing_hdr_code = :financingHdrCode AND a.agreement_code = :agreementCode
        """, nativeQuery = true)
    Optional<Map<String, Object>> findCwrCodeAndDate(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

    @Query("SELECT a.facility FROM Agreement a WHERE a.financingHdr.financingHdrCode = :financingHdrCode AND a.agreementCode = :agreementCode")
    String findFaciltyByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

//    @Query(value = """
//        SELECT i.invoice_due_date AS invoiceDueDate
//        FROM agreement a
//        JOIN cwr w ON a.cwr_code = w.cwr_code
//        JOIN customer c ON w.cust_code = c.cust_code
//        JOIN invoice i ON c.cust_code = i.cust_code
//        WHERE a.financing_hdr_code = :financingHdrCode AND a.agreement_code = :agreementCode
//        ORDER BY i.invoice_due_date DESC
//        LIMIT 1
//        """, nativeQuery = true)
//    Optional<Date> findInvoiceDueDateByFinancingHdrCode(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

    @Query(value = """
        SELECT 
            c.cust_id_no,
            c.cust_email,
            cc.cust_company_type,
            cc.company_address,
            cc.phone
        FROM agreement a
        JOIN cwr w ON a.cwr_code = w.cwr_code
        JOIN customer c ON w.cust_code = c.cust_code
        JOIN customer_company cc ON w.cust_code = cc.cust_code
        WHERE a.financing_hdr_code = :financingHdrCode AND a.agreement_code = :agreementCode
        """, nativeQuery = true)
    Optional<Map<String, Object>> finddetailDebtor(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

//    @Query(value = """
//        SELECT DISTINCT
//            i.cust_inv_no,
//            i.invoice_date,
//            i.invoice_due_date,
//            i.invoice_amt
//        FROM agreement a
//        JOIN cwr w ON a.cwr_code = w.cwr_code
//        JOIN invoice i ON w.cust_code = i.cust_code
//        WHERE a.financing_hdr_code = :financingHdrCode AND a.agreement_code = :agreementCode
//        LIMIT 10
//        """, nativeQuery = true)
//    List<Map<String, Object>> finddetailInv(@Param("financingHdrCode") UUID financingHdrCode, String agreementCode);

    @Query("SELECT a FROM Agreement a " +
            "JOIN FETCH a.cwr " +
            "JOIN FETCH a.cwr.customer " +
            "JOIN FETCH a.financingHdr " +
            "WHERE a.financingHdr.financingHdrCode = :financingHdrCode")
    Optional<Agreement> findCwr(@Param("financingHdrCode") UUID financingHdrCode);

    @Query("SELECT a.agreementCode, a.cwr.cwrCode FROM Agreement a WHERE a.agreementCode IN :agreementCodes")
    List<Object[]> findCwrCodesByAgreementCodes(@Param("agreementCodes") List<String> agreementCodes);

    List<Agreement> findByCwr_CwrCode(String cwrCode);
}

