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
    public String searchBy;;
    public String searchValue;
    public Integer pageNo;
    public Integer pageSize;
}
