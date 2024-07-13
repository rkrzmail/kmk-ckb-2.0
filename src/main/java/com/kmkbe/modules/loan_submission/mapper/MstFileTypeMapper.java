package com.kmkbe.modules.loan_submission.mapper;

import com.kmkbe.modules.loan_submission.dto.MstFileTypeDto;
import com.kmkbe.modules.loan_submission.entity.MstFileType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MstFileTypeMapper {
    MstFileTypeMapper INSTANCE = Mappers.getMapper(MstFileTypeMapper.class);

    @Mapping(target = "fileAllocation", source = "fileAllocation")
    MstFileTypeDto entityToDto(MstFileType mstFileType);
}
