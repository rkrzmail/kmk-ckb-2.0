package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Agreement;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgreementRepository extends JpaRepository<Agreement, String>, JpaSpecificationExecutor<Agreement> {
    Optional<Agreement> findTopByAgreementCodeOrderByAgreementId(String agreementCode);

    @Query(value = "SELECT * FROM public.agreement WHERE approval_flag is null OR approval_flag in ('','false') ;  ", nativeQuery = true)
    List<Agreement> viewApprovalStatusPending();
}

