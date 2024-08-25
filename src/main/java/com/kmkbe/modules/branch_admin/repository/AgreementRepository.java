package com.kmkbe.modules.branch_admin.repository;

import com.kmkbe.modules.branch_admin.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementRepository extends JpaRepository<Agreement, String> {
}
