package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FinancialDataRequest {
    @JsonProperty("TrxNo")
    private String TrxNo;

    @JsonProperty("RequestDateTime")
    private String RequestDateTime;
}

