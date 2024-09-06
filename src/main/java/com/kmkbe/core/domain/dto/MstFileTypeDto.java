package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.kmkbe.core.domain.constant.FileAllocationType;
import lombok.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@AllArgsConstructor
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
