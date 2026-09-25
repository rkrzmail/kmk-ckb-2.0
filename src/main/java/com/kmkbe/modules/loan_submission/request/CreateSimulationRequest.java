package com.kmkbe.modules.loan_submission.request;

import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.helpers.base.BaseRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSimulationRequest  extends BaseRequest {

  private String vendorCode;

  private String bouwheerCode;

  private Long productId;

  @NotNull(message = "Disburse percentage is required")
  private Double disbursePercentage;

  private Double totalInvoiceAmount;

  private transient List<PostedInvoicePayload> invoices;
}
