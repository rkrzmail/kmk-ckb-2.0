package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.LegalFileDto;
import com.kmkbe.core.domain.dto.MstFileTypeDto;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FileTypeMapper {
    FileTypeMapper INSTANCE = Mappers.getMapper(FileTypeMapper.class);

    @Mapping(target = "fileAllocation", source = "fileAllocation")
    @Mapping(ignore = true, target = "legalFile")
    MstFileTypeDto mstFileToDto(MstFileType mstFileType);

    @Mapping(ignore = true, target = "fileUrl")
    @Mapping(ignore = true, target = "uploadedDate")
    LegalFileDto legalFileToDto(LegalFile legalFile);
}
