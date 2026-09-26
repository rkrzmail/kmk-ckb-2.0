package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CwrListBwhrRequest {
    @JsonProperty("CwrNo")
    private String cwrNo;

    @JsonProperty("CwrBouwheerCustNo")
    private String cwrBouwheerCustNo;

    @JsonProperty("RequestDateTime")
    private String requestDateTime;
}

