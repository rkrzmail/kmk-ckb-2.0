package com.kmkbe.core.domain.repository;


import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiSbuRepository extends JpaRepository<ApiSbu, Long> {

    // Cari by app_key (dari header ApiKey)
    Optional<ApiSbu> findByAppKey(String appKey);

    // Cari by app_key dan hanya yang masih aktif (ses_status = 'ACTIVE')
    Optional<ApiSbu> findByAppKeyAndSesStatus(String appKey, String sesStatus);

    Optional<ApiSbu> findByBouwheerCode(UUID bouwheerCode);

    // Cari by token_jwt yang ada di path
    Optional<ApiSbu> findByTokenJwt(String tokenJwt);

    // Cari by app_key + cek expired_date masih valid
    @Query("SELECT a FROM ApiSbu a WHERE a.appKey = :appKey " +
            "AND (a.expiredDate IS NULL OR a.expiredDate > CURRENT_TIMESTAMP)")
    Optional<ApiSbu> findActiveByAppKey(@Param("appKey") String appKey);
}
