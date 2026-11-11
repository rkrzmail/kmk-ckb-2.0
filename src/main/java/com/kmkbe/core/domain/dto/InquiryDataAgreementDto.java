package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryDataAgreementDto {
    @JsonProperty("AgrmntObj")
    public InquiryDataAgreementDtoAgrmntObj agrmntObj;
    @JsonProperty("HeaderObj")
    public InquiryHeaderObj headerObj;
    @JsonProperty("StatusCode")
    public String statusCode;
    @JsonProperty("Message")
    public String message;
    @JsonProperty("ErrorMessages")
    public Object errorMessages;
    @JsonProperty("RowVersion")
    public Object rowVersion;

}


