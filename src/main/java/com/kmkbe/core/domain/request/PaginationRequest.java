package com.kmkbe.core.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
  @Schema(type = "string", pattern = "dd/MM/yyyy", example = "26/06/2026")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "Asia/Jakarta")
  private Date startDate;

  @Schema(type = "string", pattern = "dd/MM/yyyy", example = "26/06/2026")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "Asia/Jakarta")
  private Date endDate;
  private String searchBy;
  private String searchValue;
  private Integer pageNo;
  private Integer pageSize;
}
