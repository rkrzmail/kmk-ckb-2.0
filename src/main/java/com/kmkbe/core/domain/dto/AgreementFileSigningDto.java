package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@NoArgsConstructor
public class AgreementFileSigningDto implements Serializable {
    private Long agreementFileId;
    private String agreementCode;
    private String fileTypeCode;
    private String fileName;
    private String stamp;
    private String usrCrt;
    private String dtmCrt;
//    private String usrUpd;
//    private String dtmUpd;
    private String documentId;
    private String identityNo;
    private String financingHdrCode;
    private String emailSigner;

}
