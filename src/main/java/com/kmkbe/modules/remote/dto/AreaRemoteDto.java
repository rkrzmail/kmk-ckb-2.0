package com.kmkbe.modules.remote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AreaRemoteDto {
    @JsonProperty("AreaCode1")
    public String areaCode1;

    @JsonProperty("AreaCode2")
    public String areaCode2;

    @JsonProperty("Zipcode")
    public String zipcode;

    @JsonProperty("City")
    public String city;

    @JsonProperty("Province")
    public String province;

    @JsonProperty("RefZipcodeId")
    public Integer refZipcodeId;

    @JsonProperty("IsActive")
    public Boolean isActive;

    @JsonProperty("SubZipcode")
    public String subZipcode;

    @JsonProperty("PhnArea")
    public String phnArea;
}
