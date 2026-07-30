package com.kmkbe.helpers.base;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BasePaginationRequest extends BaseRequest{
  @NotNull(message = "Page size is required")
  @Min(value = 1, message = "Page size must be at least 1")
  private Integer pageSize;

  @NotNull(message = "Page number is required")
  @Min(value = 1, message = "Page number must be greater than or equal to 1")
  private Integer pageNo;

  @NotBlank(message = "Sort by field is required")
  private String sortBy;

  @NotBlank(message = "Sort type is required")
  @Pattern(
    regexp = "^(?i)(ASC|DESC)$",
    message = "Sort type must be either 'ASC' or 'DESC'"
  )
  private String sortType;

  @NotBlank(message = "Search by field is required")
  private String searchBy;

  private String searchValue;
}
