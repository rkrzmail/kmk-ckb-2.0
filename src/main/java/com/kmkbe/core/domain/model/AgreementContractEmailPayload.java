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
  private String vendorEmail;
  private String vendorPhone;
  private String submissionDate;
  private String cwrCode;
  private String totalInvoiceAmt;
  private String retention;
  private String financingAmt;
  private String totalFeeAmt;
  private String tenor;
  private String disburseAmt;
  private String invoices;
}
