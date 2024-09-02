package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.modules.user.entity.MstBranch;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FinancingHdrRepository extends JpaRepository<FinancingHdr, UUID>, JpaSpecificationExecutor<FinancingHdr> {
    Optional<FinancingHdr> findByFinancingHdrCode(UUID code);

    Optional<FinancingHdr> findFirstByCustomerOrderByFinancingHdrIdDesc(Customer customer);

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
                        fh.financing_hdr_id desc
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
                        fh.financing_hdr_code in (
                                                     select
                                                         fd.financing_hdr_code
                                                     from
                                                         public.financing_dtl fd
                                                             join public.invoice iv on fd.invoice_code = iv.invoice_code
                                                 )
                    order by
                        fh.financing_hdr_id desc
                    """,
            countQuery = """
                    select count(*)
                    from
                        financing_hdr fh
                    where
                        fh.financing_hdr_code in (
                                                     select
                                                         fd.financing_hdr_code
                                                     from
                                                         public.financing_dtl fd
                                                             join public.invoice iv on fd.invoice_code = iv.invoice_code
                                                 )
                    """,
            nativeQuery = true
    )
    Page<FinancingHdr> findAllByRawOrder(
            Pageable pageable
    );
}
