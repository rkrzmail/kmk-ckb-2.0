package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSITDto {
    private String rt;
    private String rw;
    private String kelurahan;
    private String kecamatan;
    private String city;
    private String province;
    private String zipCode;
    private String area;
    private String namaBank;

}
