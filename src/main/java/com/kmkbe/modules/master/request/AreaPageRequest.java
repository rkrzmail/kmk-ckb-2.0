package com.kmkbe.modules.master.request;

import com.kmkbe.helpers.base.BaseRequest;
import com.kmkbe.modules.confinsr3.model.request.ConfinsR3ZipcodeCriteriaRequest;
import lombok.Data;

import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class AreaPageRequest extends BaseRequest {
  @NotNull(message = "Page number is required")
  @Min(value = 1, message = "Page number must be greater than or equal to 1")
  private Integer pageNo;

  @NotNull(message = "Page size is required")
  @Min(value = 1, message = "Page size must be at least 1")
  private Integer pageSize;

  private List<ConfinsR3ZipcodeCriteriaRequest> criteria;
}

