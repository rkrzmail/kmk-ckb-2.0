package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingHdr;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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






    List<Agreement> findAllByAgreementCode(  String agreementCode);

    List<Agreement> findAllByCwr(@NotNull(message = "Cwr cannot be null") Cwr cwr);

    Optional<Agreement> findTopByFinancingHdr(FinancingHdr financingHdr);
    List<Agreement> findByFinancingHdr_FinancingHdrCode(UUID financinghdrCode);

    //List<Agreement> findAllByStatus(@Size(max = 20) @NotNull(message = "Status cannot be null") String status);


    @Query("SELECT a FROM Agreement a " +
            "JOIN FETCH a.cwr " +
            "JOIN FETCH a.cwr.customer " +
            "WHERE a.financingHdr.financingHdrCode = :financingHdrCode") // Nama parameter harus sama
    Optional<Agreement> findByFinancingHdr_FinancingHdrCode2(@Param("financingHdrCode") UUID financingHdrCode);
}

