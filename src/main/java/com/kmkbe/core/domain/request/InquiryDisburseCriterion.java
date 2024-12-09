package com.kmkbe.core.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDisburseCriterion {
    public int low;
    public int high;
    @JsonProperty("DataType")
    public String dataType;
    public boolean isCriteriaDataTable;
    public String restriction;
    public String propName;
    public String value;
}
