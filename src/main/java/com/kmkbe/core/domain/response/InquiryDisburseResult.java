package com.kmkbe.core.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.core.domain.dto.InquiryDataAgreementDtoAgrmntObj;
import com.kmkbe.core.domain.dto.InquiryHeaderObj;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryDisburseResult {
    @JsonProperty("Data")
    public ArrayList<InquiryDisburseDatum> data;
    @JsonProperty("Count")
    public int count;
    @JsonProperty("HeaderObj")
    public InquiryDisburseHeaderObj headerObj;
    @JsonProperty("StatusCode")
    public String statusCode;
    @JsonProperty("Message")
    public String message;
    @JsonProperty("ErrorMessages")
    public Object errorMessages;
    @JsonProperty("RowVersion")
    public Object rowVersion;

}


