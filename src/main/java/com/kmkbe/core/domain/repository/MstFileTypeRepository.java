package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.MstFileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface MstFileTypeRepository extends JpaRepository<MstFileType, String> {
    Optional<MstFileType> findByFileTypeCode(String code);

    Optional<List<MstFileType>> findAllByIsMandatory(Boolean isMandatory);

    default List<MstFileType> findAllMandatory() {
        return findAllByIsMandatory(true).orElse(new ArrayList<>());
    }

    Optional<MstFileType> findTopByFileTypeNameOrderByFileTypeIdDesc(String fileTypeName);

    Optional<MstFileType> findToByOrderByFileTypeIdDesc();
}
