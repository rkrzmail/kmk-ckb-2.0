package com.kmkbe.core.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDataAgreementRequest {
    @JsonProperty("TRXNO")
    public String trxNo;
    @JsonProperty("REQUESTDATETIME")
    public String requestDateTime;

}
