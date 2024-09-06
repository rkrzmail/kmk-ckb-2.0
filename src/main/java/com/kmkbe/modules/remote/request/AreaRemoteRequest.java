package com.kmkbe.modules.remote.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaRemoteRequest {
    private PropCriteriaGenericTypeRequest.AreaPropName type;
    private List<PropCriteriaGenericTypeRequest.AreaPropName> areas;
    private String value;

    @Builder.Default
    private Integer pageNo = 1;

    @Builder.Default
    private Integer rowPerPage = 10;
}
