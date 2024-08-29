package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface LegalFileRepository extends JpaRepository<LegalFile, Long>, JpaSpecificationExecutor<LegalFile> {
    List<LegalFile> findAllByCustCode(Customer customer);

    Optional<LegalFile> findByCustCodeAndFileTypeCode(Customer customer, MstFileType fileType);

    Optional<LegalFile> findTopByFileNameOrderByFileIdDesc(String fileName);

    Optional<LegalFile> findTopByFileIdOrderByFileIdDesc(Long fileId);

    Page<LegalFile> findAllByCustCodeOrderByFileName(
            Customer customer,
            Pageable pageable
    );

}
