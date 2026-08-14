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

  @NotNull(message = "Vendor Code is required")
  private String vendorCode;

  @NotNull(message = "Bouwheer is required")
  private String bouwheerCode;

  private Long productId;

  @NotNull(message = "Disburse percentage is required")
  private Double disbursePercentage;

  @NotNull(message = "Total invoice amount required")
  @Min(value = 50000000, message = "Untuk melanjutkan pengajuan silahkan tambahkan jumlah invoice yang ingin diajukan hingga mencapai minimal Rp 50.000.000")
  private Double totalInvoiceAmount;

  private transient List<PostedInvoicePayload> invoices;
}
