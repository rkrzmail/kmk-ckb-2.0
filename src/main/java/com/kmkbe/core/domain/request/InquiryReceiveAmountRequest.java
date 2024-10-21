package com.kmkbe.core.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryReceiveAmountRequest {
    @JsonProperty("RcvAmtList")
    public ArrayList<RcvAmtList> rcvAmtList;
    @JsonProperty("ValueDt")
    public String valueDt;
    @JsonProperty("RequestDateTime")
    public String requestDateTime;

    public static class RcvAmtList{
        @JsonProperty("AgrmntNo")
        public String agrmntNo;
        @JsonProperty("RcvAmt")
        public double rcvAmt;
    }


}
