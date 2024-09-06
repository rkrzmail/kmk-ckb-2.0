package com.kmkbe.core.domain.request;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    private Date startDate = new Date();
    private Date endDate = new Date();
    private String searchBy;;
    private String searchValue;
    private Integer pageNo;
    private Integer pageSize;
}
