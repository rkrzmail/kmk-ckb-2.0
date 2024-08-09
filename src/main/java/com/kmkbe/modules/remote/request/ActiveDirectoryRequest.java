package com.kmkbe.modules.remote.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Builder
@JsonPropertyOrder({"LoginID", "Password", "ApplicationScheme"})
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class ActiveDirectoryRequest {
    @Builder.Default
    @JsonProperty("LoginID")
    private String loginID = "csul.staging";

    @Builder.Default
    @JsonProperty("Password")
    private String password = "Welcome#2024";

    @Builder.Default
    @JsonProperty("ApplicationScheme")
    private String applicationScheme = "";
}
