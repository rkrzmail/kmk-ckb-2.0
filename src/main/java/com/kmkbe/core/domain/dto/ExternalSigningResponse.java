package com.kmkbe.core.domain.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalSigningResponse {
    private Status status;
    private List<Document> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private int code;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {
        private String documentId;
        private List<String> trxNos;
        private String docTemplateCode;
        private String psreCode;
    }
}