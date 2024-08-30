package com.kmkbe.modules.remote.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAgreementRemoteRequest {
    @Builder.Default
    private PropCriteriaGenericTypeRequest.AgreementPropName name = PropCriteriaGenericTypeRequest.AgreementPropName.agreementNo;
    private String agreementNo;
}
