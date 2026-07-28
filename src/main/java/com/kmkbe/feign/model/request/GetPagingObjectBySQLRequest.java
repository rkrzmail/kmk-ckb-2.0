package com.kmkbe.feign.model.request;

import com.kmkbe.helpers.base.BaseRequest;
import lombok.*;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetPagingObjectBySQLRequest extends BaseRequest {
  private Boolean includeCount;
  private Boolean includeData;
  private Boolean isLoading;
  private transient QueryStringQueryDto queryString;
  private String rowVersion;
  private transient Object integrationObj;
  private String joinType;
  private Integer pageNo;
  private Integer rowPerPage;
  private transient Object orderBy;

  /** List of specific filtering criteria (The WHERE clauses). */
  private transient List<CriterionDto> criteria;

  private String requestDateTime;

  @Data
  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class QueryStringQueryDto {
    private String name;
    private List<String> whereQuery;
  }

  @Data
  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class CriterionDto {
    private Integer low;
    private Integer high;
    private String dataType;
    private String isCriteriaDataTable;
    private String propName;
    private String value;
    private String restriction;
  }
}
