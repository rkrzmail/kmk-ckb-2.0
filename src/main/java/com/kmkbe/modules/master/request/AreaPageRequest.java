package com.kmkbe.modules.master.request;

import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.confinsr3.model.request.ConfinsR3ZipcodeCriteriaRequest;
import lombok.Data;

import java.util.List;

@Data
public class AreaPageRequest extends BasePaginationRequest {
  private List<ConfinsR3ZipcodeCriteriaRequest> criteria;
}

