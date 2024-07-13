package com.kmkbe.core.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class PaginationPayload {
    public Integer pageNo;
    public Integer pageSize;
}
