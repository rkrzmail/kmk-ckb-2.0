package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AreaRemoteDto {
    @JsonAlias("AreaCode1")
    public String kecamatan;

    @JsonAlias("AreaCode2")
    public String kelurahan;

    @JsonAlias("Zipcode")
    public String zipcode;

    @JsonAlias("City")
    public String city;

    @JsonAlias("Province")
    public String province;

    @JsonAlias("RefZipcodeId")
    public Integer refZipcodeId;

    @JsonAlias("IsActive")
    public Boolean isActive;

    @JsonAlias("SubZipcode")
    public String subZipcode;

    @JsonAlias("PhnArea")
    public String phnArea;
}
