package com.kmkbe.modules.customer.model.request;


import com.kmkbe.helpers.base.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author hyvercode
 * @date 6/26/26
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequest extends BaseRequest {
  @NotNull(message = "Customer Code is required")
  UUID custCode;

  @NotBlank(message = "Status cannot be empty")
  @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "Status must be APPROVED or REJECTED")
  String approvalStatus;

  String approvalNote;
}
