package com.kmkbe.core.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.core.domain.dto.InquiryDataAgreementDtoAgrmntObj;
import com.kmkbe.core.domain.dto.InquiryHeaderObj;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryDisburseHeaderObj {
    @JsonProperty("ResponseTime")
    public String responseTime;
    @JsonProperty("StatusCode")
    public String statusCode;
    @JsonProperty("Message")
    public String message;
    @JsonProperty("ErrorMessages")
    public Object errorMessages;



}


