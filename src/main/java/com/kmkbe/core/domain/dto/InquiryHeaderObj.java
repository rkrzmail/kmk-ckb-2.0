package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryHeaderObj {
    @JsonProperty("ResponseTime")
    public String responseTime;
    @JsonProperty("StatusCode")
    public String statusCode;
    @JsonProperty("Message")
    public String message;
    @JsonProperty("ErrorMessages")
    public Object errorMessages;
}


