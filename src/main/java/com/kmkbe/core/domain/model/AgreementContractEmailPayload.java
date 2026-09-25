package com.kmkbe.core.domain.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AgreementContractEmailPayload {
  private String agreementCode;
  private String financingCode;
  private String vendorCode;
  private String vendorName;
  private String bouwheerName;
  private String bouwheerPicEmails;
  private String branchName;
}
