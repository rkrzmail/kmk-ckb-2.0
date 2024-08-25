package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Cwr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CwrRepository extends JpaRepository<Cwr, String>, JpaSpecificationExecutor<Cwr> {
}
