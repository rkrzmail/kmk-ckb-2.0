package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExistingCustomerDto {
    @JsonIgnore
    private Integer id;
    private String vendorCode;
    private String identityType;
    private String identityNo;
    private Boolean isExisting;
    private Instant dtmCrt;
    private Instant dtmUpd;
}
