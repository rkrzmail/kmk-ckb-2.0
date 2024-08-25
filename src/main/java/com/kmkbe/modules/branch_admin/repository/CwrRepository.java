package com.kmkbe.modules.branch_admin.repository;

import com.kmkbe.modules.branch_admin.entity.Cwr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CwrRepository extends JpaRepository<Cwr, String>, JpaSpecificationExecutor<Cwr> {
}
