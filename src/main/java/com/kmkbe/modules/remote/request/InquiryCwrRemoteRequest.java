package com.kmkbe.modules.remote.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCwrRemoteRequest {
    @Builder.Default
    private PropCriteriaGenericTypeRequest.CwrPropName name = PropCriteriaGenericTypeRequest.CwrPropName.cwrNo;
    private String cwrNo;
}
