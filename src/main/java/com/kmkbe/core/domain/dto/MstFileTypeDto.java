package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.kmkbe.core.domain.constant.FileAllocationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class MstFileTypeDto {
    private String fileTypeCode;
    private String fileTypeName;
    private String fileTypeDesc;
    private FileAllocationType fileAllocation;
    private Boolean isMandatory;
    private Long maxSizeMb;
    private LegalFileDto legalFile;
}
