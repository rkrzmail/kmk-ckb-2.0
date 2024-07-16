package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.loan_submission.entity.LegalFile;
import com.kmkbe.modules.loan_submission.entity.MstFileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegalFileRepository extends JpaRepository<LegalFile, Long> {
    Optional<LegalFile> findByCustCode(Customer customer);

    Optional<LegalFile> findByCustCodeAndFileTypeCode(Customer customer, MstFileType fileType);
}
