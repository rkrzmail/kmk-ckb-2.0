package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Builder
@Getter
public class AgreementListDto implements Serializable {

    private String agreementNo;

}
