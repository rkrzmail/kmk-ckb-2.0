package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class AgreementListDto implements Serializable {

    private String agreementNo;

}
