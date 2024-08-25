package com.kmkbe.core.domain.model;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResult<T> {
    private Integer currentPage;
    private Integer totalPage;
    private Long totalData;
    private List<T> list;
}
