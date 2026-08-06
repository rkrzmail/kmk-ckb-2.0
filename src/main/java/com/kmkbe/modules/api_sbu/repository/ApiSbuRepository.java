package com.kmkbe.modules.api_sbu.repository;


import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiSbuRepository extends JpaRepository<ApiSbu, Long> {

    Optional<ApiSbu> findByAppKey(String appKey);

    Optional<ApiSbu> findByAppKeyAndSesStatus(String appKey, String sesStatus);

    Optional<ApiSbu> findByBouwheerCode(UUID bouwheerCode);

    Optional<ApiSbu> findByTokenJwt(String tokenJwt);

    @Query("SELECT a FROM ApiSbu a WHERE a.appKey = :appKey " +
            "AND (a.expiredDate IS NULL OR a.expiredDate > CURRENT_TIMESTAMP)")
    Optional<ApiSbu> findActiveByAppKey(@Param("appKey") String appKey);

   Page<ApiSbu> findAll(Specification<ApiSbu> specification, Pageable pageable);

   Optional<ApiSbu> findByBouwheerCodeAndAppName(UUID code, String appName);
}
