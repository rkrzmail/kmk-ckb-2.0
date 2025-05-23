package com.kmkbe.core.domain.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date startDate;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date endDate;
    private String searchBy;;
    private String searchValue;
    private Integer pageNo;
    private Integer pageSize;
}
