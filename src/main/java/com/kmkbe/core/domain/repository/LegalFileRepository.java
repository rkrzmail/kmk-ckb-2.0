package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegalFileRepository extends JpaRepository<LegalFile, Long> {
    Optional<LegalFile> findByCustCode(Customer customer);

    Optional<LegalFile> findByCustCodeAndFileTypeCode(Customer customer, MstFileType fileType);
}
