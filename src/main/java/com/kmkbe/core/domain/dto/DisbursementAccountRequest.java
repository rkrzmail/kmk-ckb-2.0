package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DisbursementAccountRequest(
    @JsonProperty("AgrmntNo") String agreementNo,
    @JsonProperty("RequestDateTime") String requestDateTime
) {}
