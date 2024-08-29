package com.kmkbe.modules.remote.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ZipCodeRequest {
    private Integer pageNo;
    private Integer rowPerPage;
    private String city;
    private String kecamatan;
    private String kelurahan;
    private String zipCode;
}
