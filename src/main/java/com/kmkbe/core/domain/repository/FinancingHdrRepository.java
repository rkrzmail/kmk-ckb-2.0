package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.modules.user.entity.MstBranch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<FinancingHdr> findByFinancingStatusAndMstBranchOrderByFinancingHdrIdDesc(
            String financingStatus,
            MstBranch mstBranch,
            Pageable pageable
    );

    @Query(
            value = """
                    select *
                        from
                            financing_hdr
                        order by
                            financing_hdr.financing_hdr_id desc
                    """,
            countQuery = """
                    select count(*)
                    from
                        financing_hdr
                    """,
            nativeQuery = true
    )
    Page<FinancingHdr> findAllByRawOrder(
            Pageable pageable
    );
}
