package com.kmkbe.modules.loan_submission.mapper;

import com.kmkbe.modules.loan_submission.dto.LegalFileDto;
import com.kmkbe.modules.loan_submission.dto.MstFileTypeDto;
import com.kmkbe.modules.loan_submission.entity.LegalFile;
import com.kmkbe.modules.loan_submission.entity.MstFileType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FileTypeMapper {
    FileTypeMapper INSTANCE = Mappers.getMapper(FileTypeMapper.class);

    @Mapping(target = "fileAllocation", source = "fileAllocation")
    @Mapping(ignore = true, target = "legalFile")
    MstFileTypeDto mstFileToDto(MstFileType mstFileType);

    LegalFileDto legalFileToDto(LegalFile legalFile);
}
