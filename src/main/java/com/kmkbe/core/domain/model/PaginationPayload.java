package com.kmkbe.core.domain.model;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationPayload {
    public Integer pageNo;
    public Integer pageSize;
}
