package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.GeneralSettingHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GeneralSettingHdrRepository extends JpaRepository<GeneralSettingHdr, String>, JpaSpecificationExecutor<GeneralSettingHdr> {
}
