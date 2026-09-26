package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppFactoringRequest {
    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("RequestDateTime")
    private String requestDateTime;
}

