package com.kmkbe.helpers.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pagination implements Serializable {
  private Integer pageSize;
  private Integer currentPage;
  private Integer totalPages;
  private Long totalRecords;
  private Boolean isFirstPage;
  private Boolean isLastPage;
}
