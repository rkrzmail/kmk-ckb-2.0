package com.kmkbe.core.domain.repository;

import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LegalFileRepository extends JpaRepository<LegalFile, Long>, JpaSpecificationExecutor<LegalFile> {
    boolean existsByFileTypeCode_FileTypeCode(String fileTypeCode);
    List<LegalFile> findAllByCustCode(Customer customer);

    Optional<LegalFile> findByCustCodeAndFileTypeCodeAndFinancingHdrIsNull(
            Customer customer, MstFileType fileType);

    @Query(value = """
            select lf.* from public.legal_file lf
            where lf.financing_hdr_code = :financingHdrCode
              and lf.file_type_code = :fileTypeCode
            """, nativeQuery = true)
    Optional<LegalFile> findByFinancingHdr_FinancingHdrCodeAndFileTypeCode_FileTypeCode(
            @org.springframework.data.repository.query.Param("financingHdrCode") UUID financingHdrCode,
            @org.springframework.data.repository.query.Param("fileTypeCode") String fileTypeCode);

    @Query(
            value = """
                    select *
                    from
                        public.legal_file
                    where
                        file_type_code = :fileTypeCode
                        and cust_code = :custCode
                        and financing_hdr_code is null
                    order by
                        file_id desc limit 1
                    """,
            nativeQuery = true
    )
    List<LegalFile> findAllRawByCustAndFileTypeCodeStr(
            @Param("fileTypeCode") String fileTypeCode,
            @Param("custCode") String custCode
    );

    Optional<LegalFile> findTopByFileIdOrderByFileIdDesc(Long fileId);

    Page<LegalFile> findAllByCustCodeAndFinancingHdrIsNullOrderByFileName(
            Customer customer,
            Pageable pageable
    );

}
