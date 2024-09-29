package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
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

    Optional<FinancingDtl> findFirstByBouwheerInvNo(String bouwheerInvNo);

    @Query(
            value = "select fd.* \n" +
                    "from\n" +
                    "    financing_dtl as fd\n" +
                    "where\n" +
                    "    financing_hdr_code = (\n" +
                    "                             select financing_hdr_code\n" +
                    "                             from financing_hdr\n" +
                    "                             where cust_code = :custCode\n" +
                    "                             order by financing_hdr_id desc limit 1\n" +
                    "                         ) " +
                    "order by ?#{#pageable}",
            countQuery = "select count(*) \n" +
                    "from\n" +
                    "    financing_dtl as fd\n" +
                    "where\n" +
                    "    financing_hdr_code = (\n" +
                    "                             select financing_hdr_code\n" +
                    "                             from financing_hdr\n" +
                    "                             where cust_code = :custCode\n" +
                    "                             order by financing_hdr_id desc limit 1\n" +
                    "                         )",
            nativeQuery = true
    )
    Page<FinancingDtl> findByCustomer(
            String custCode,
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
}
