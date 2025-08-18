package com.kmkbe.core.domain.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalSigningRequest {
    private String tenantCode;
    private String psreCode;
    private Audit audit;
    private List<DocumentRequest> requests;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentRequest {
        private String referenceNo;
        private String documentTemplateCode;
        private String documentName;
        private String officeCode;
        private String officeName;
        private String regionCode;
        private String regionName;
        private String businessLineCode;
        private String businessLineName;
        private List<Signer> signers;
        private String documentFile;
        private String isSequence;
        private String useSignQR;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Signer {
        private String signAction;
        private String signerType;
        private String idKtp;
        private String tlp;
        private String email;
        private String seqNo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Audit {
        private String callerId;
    }
}