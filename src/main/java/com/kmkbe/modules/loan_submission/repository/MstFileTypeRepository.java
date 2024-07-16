package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.loan_submission.entity.MstFileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MstFileTypeRepository extends JpaRepository<MstFileType, String> {
    Optional<MstFileType> findByFileTypeCode(String code);
}
