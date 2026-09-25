package com.kmkbe.modules.major_account.request;

import com.kmkbe.helpers.base.BasePaginationRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class DistributionSubmissionListRequest extends BasePaginationRequest {
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private LocalDate startDate;

  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private LocalDate endDate;
}
