package com.kmkbe.core.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDisburseRequest {
    public boolean includeCount;
    public boolean includeData;
    public boolean isLoading;
    public InquiryDisburseQueryString queryString;
    public String rowVersion;
    public Object integrationObj;
    public String joinType;
    public int pageNo;
    public int rowPerPage;
    public InquiryDisburseOrderBy orderBy;
    public ArrayList<InquiryDisburseCriterion> criteria;
    @JsonProperty("RequestDateTime")
    public String requestDateTime;
}
