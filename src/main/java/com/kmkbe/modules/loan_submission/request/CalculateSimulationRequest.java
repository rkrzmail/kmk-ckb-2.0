package com.kmkbe.modules.loan_submission.request;

import com.kmkbe.helpers.base.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateSimulationRequest extends BaseRequest {
  @NotNull(message = "Token is required")
  private String token;
  @NotNull(message = "Bouwheer is required")
  private String bouwheerCode;
  private BigDecimal totalInvoiceAmount;
  private Double disbursePercentage;
  private String invoiceDueDate;
  private Double interest;
}
