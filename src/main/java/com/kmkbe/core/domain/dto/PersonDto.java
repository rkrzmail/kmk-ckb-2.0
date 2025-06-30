package com.kmkbe.core.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class PersonDto {
    private List<Signer> signers;
    private String statusCode;
    private String message;

    @Data
    public static class Signer {
        private Integer cwrSignerId;
        private Integer cwrCustId;
        private String signerType;
        private String signerName;
        private String signerPosition;
    }
}
