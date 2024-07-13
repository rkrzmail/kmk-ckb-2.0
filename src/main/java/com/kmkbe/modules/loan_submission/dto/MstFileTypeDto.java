package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.kmkbe.modules.loan_submission.constant.FileAllocationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

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
    private String usrCrt;
    private Instant dtmCrt;
    private String usrUpd;
    private Instant dtmUpd;
}
