package com.kmkbe.modules.loan_submission.request;

import com.kmkbe.helpers.base.BasePaginationRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class FinancingInvoicePaginationRequest extends BasePaginationRequest {
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private Date startDate;

  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private Date endDate;
}
