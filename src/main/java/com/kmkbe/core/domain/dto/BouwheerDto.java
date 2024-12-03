package com.kmkbe.core.domain.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class BouwheerDto {
    private UUID bouwheerCode;
    private String bouwheerName;
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
    private Boolean isSbu;
    private String picName;
    private String picEmail;
    private String picMobilePhone;
    private Boolean isWaActive;
    private Long termOfPayment;
    private Long gracePeriod;
    private String aesKey;
    private Boolean isActive;
    private String usrCrt;
    private LocalDateTime dtmCrt;
    private String usrUpd;
    private LocalDateTime dtmUpd;
}
