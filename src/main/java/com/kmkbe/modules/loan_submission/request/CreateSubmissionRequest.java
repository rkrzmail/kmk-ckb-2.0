package com.kmkbe.modules.loan_submission.request;

import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.helpers.base.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubmissionRequest extends BaseRequest {

  @NotNull(message = "Vendor Code is required")
  private String vendorCode;

  @NotNull(message = "Bouwheer is required")
  private String bouwheerCode;

  private Long productId;

  @NotNull(message = "Disburse percentage is required")
  private Double disbursePercentage;

  private Double totalInvoiceAmount;

  private transient List<PostedInvoicePayload> invoices;
}
