package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.MstFileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MstFileTypeRepository extends JpaRepository<MstFileType, String>, JpaSpecificationExecutor<MstFileType> {
  Optional<MstFileType> findByFileTypeCodeAndBouwheerCode(String code,UUID bouwheerCode);

    Optional<List<MstFileType>> findAllByIsMandatory(Boolean isMandatory);

    default List<MstFileType> findAllMandatory() {
        return findAllByIsMandatory(true).orElse(new ArrayList<>());
    }

    Optional<MstFileType> findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc(String fileTypeName, UUID bouwheerCode);

    Page<MstFileType> findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(
            List<String> fileAllocation,
            UUID bouwheerCode,
            Pageable pageable
    );

    List<MstFileType> findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(List<String> fileAllocation, UUID bouwheerCode);

}
