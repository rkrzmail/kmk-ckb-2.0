package com.kmkbe.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse extends BaseResponse {
  private Integer code;
  private String title;
  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<ErrorValidationResponse> validations;
}
