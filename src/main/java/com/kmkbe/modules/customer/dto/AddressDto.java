package com.kmkbe.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private String rt;
    private String rw;
    private String kelurahan;
    private String kecamatan;
    private String city;
    private String province;
    private String zipCode;
    private String area;
}
