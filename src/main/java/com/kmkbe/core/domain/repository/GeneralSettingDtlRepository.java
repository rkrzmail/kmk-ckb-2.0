package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.GeneralSettingDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GeneralSettingDtlRepository extends JpaRepository<GeneralSettingDtl, String>, JpaSpecificationExecutor<GeneralSettingDtl> {
    Optional<GeneralSettingDtl> findTopByGsDtlCode(String code);
}
