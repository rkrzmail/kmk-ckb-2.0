package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CwrListBwhrRequest {
    @JsonProperty("CwrNo")
    private String CwrNo;

    @JsonProperty("CwrBouwheerCustNo")
    private String CwrBouwheerCustNo;

    @JsonProperty("RequestDateTime")
    private String RequestDateTime;
}

