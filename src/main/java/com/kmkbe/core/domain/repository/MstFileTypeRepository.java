package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.MstFileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface MstFileTypeRepository extends JpaRepository<MstFileType, String>, JpaSpecificationExecutor<MstFileType> {
    Optional<MstFileType> findByFileTypeCode(String code);

    Optional<List<MstFileType>> findAllByIsMandatory(Boolean isMandatory);

    default List<MstFileType> findAllMandatory() {
        return findAllByIsMandatory(true).orElse(new ArrayList<>());
    }

    Optional<MstFileType> findTopByFileTypeNameOrderByFileTypeIdDesc(String fileTypeName);

    Optional<MstFileType> findToByOrderByFileTypeIdDesc();

    Page<MstFileType> findAllByFileAllocationInOrderByFileTypeIdDesc(
            List<String> fileAllocation,
            Pageable pageable
    );

    List<MstFileType> findAllByFileAllocationInOrderByFileTypeIdDesc(List<String> fileAllocation);

}
