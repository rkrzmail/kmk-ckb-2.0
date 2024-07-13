package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.loan_submission.entity.LegalFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalFileRepository extends JpaRepository<LegalFile, Long> {
}
