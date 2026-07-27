package com.kmkbe.modules.loan_submission.request;

import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.helpers.base.BaseRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSimulationRequest  extends BaseRequest {
  @NotNull(message = "Bouwheer is required")
  private String bouwheerCode;
  @NotNull(message = "Product id is required")
  private Long productId;

  @NotNull(message = "Disburse percentage is required")
  private Double disbursePercentage;

  private transient List<PostedInvoicePayload> invoices;
}
