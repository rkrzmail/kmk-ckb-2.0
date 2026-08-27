package com.kmkbe.core.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryNewInfoAgreementRequest {
    @JsonProperty("AgrmntNo")
    public String agrmntNo;
    @JsonProperty("ValueDt")
    public String valueDt;
    @JsonProperty("RequestDateTime")
    public String requestDateTime;

}
