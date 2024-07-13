package com.kmkbe.core.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
public class PaginationResult<T> {
    private Integer currentPage;
    private Integer totalPage;
    private Long totalData;
    private List<T> list;
}
