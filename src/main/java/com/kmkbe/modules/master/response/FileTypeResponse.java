package com.kmkbe.modules.master.response;

import com.kmkbe.core.domain.entity.MstFileType;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileTypeResponse(
    String fileTypeCode, Long fileTypeId, String fileTypeName, String fileTypeDesc,
    String fileAllocation, Boolean isMandatory, Long maxSizeMb, UUID bouwheerCode,
    String usrCrt, LocalDateTime dtmCrt, String usrUpd, LocalDateTime dtmUpd
) {
    public static FileTypeResponse from(MstFileType entity) {
        return new FileTypeResponse(entity.getFileTypeCode(), entity.getFileTypeId(),
            entity.getFileTypeName(), entity.getFileTypeDesc(), entity.getFileAllocation(),
            entity.getIsMandatory(), entity.getMaxSizeMb(), entity.getBouwheerCode(),
            entity.getUsrCrt(), entity.getDtmCrt(), entity.getUsrUpd(), entity.getDtmUpd());
    }
}
