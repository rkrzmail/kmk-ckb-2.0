package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ExternalDownloadResponse {
    private Status status;
    private String pdfBase64;
    private List<String> eStampDutySN;

    @Data
    public static class Status {
        private int code;
        private String message;
    }
}
