package com.kmkbe.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class CustomerPersonalDto {
    //private UUID custCode;
    private String birthplace;
    private OffsetDateTime birthdate;
    private String gender;
    private String identityType;
    private String identityNo;
    private OffsetDateTime expiredDate;
    private String motherMaidenName;
    private String maritalStatus;
    private String custModel;
    private String legalAddress;
    private String rt;
    private String rw;
    private String kelurahan;
    private String kecamatan;
    private String city;
    private String province;
    private String zipcode;
    private String area;
    private String phone;
    private String ownershipStatus;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime staySince;

    private BigDecimal stayLength;
}
