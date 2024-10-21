package com.kmkbe.core.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDataAgreementRequest {
    @JsonProperty("TRXNO")
    public String trxNo;
    @JsonProperty("REQUESTDATETIME")
    public String requestDateTime;

}
