package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppRequest {
    @JsonProperty("TrxNo")
    private String trxNo;

    @JsonProperty("RequestDateTime")
    private String requestDateTime;
}

