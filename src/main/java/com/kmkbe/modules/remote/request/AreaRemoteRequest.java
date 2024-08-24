package com.kmkbe.modules.remote.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaRemoteRequest {
    private PropCriteriaGenericTypeRequest.AreaPropName area;
    private String value;

    @Builder.Default
    private Integer pageNo = 1;

    @Builder.Default
    private Integer rowPerPage = 10;
}
