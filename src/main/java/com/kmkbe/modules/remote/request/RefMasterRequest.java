package com.kmkbe.modules.remote.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.DateTimeUtils;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@Getter
public class RefMasterRequest {
    @JsonProperty("RefMasterTypeCode")
    private BaseRemoteService.RefMasterTypeCode refMasterTypeCode;

    @Builder.Default
    @JsonProperty("RequestDateTime")
    private String requestDateTime = DateTimeUtils.SDF_STANDARD_DATE.format(new Date());

    @JsonProperty("MappingCode")
    @JsonInclude(
            value = JsonInclude.Include.NON_NULL
    )
    private ModelDebiturMappingCode modelDebitur;

    public enum ModelDebiturMappingCode {
        PERSONAL, COMPANY
    }
}
