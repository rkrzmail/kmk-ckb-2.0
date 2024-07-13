package com.kmkbe.modules.external.request;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@JsonPropertyOrder({"LoginID", "Password", "ApplicationScheme"})
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class ActiveDirectoryRequest {
    private String LoginID = "csul.staging";
    private String Password = "Welcome#2024";
    private String ApplicationScheme = "";
}
