package com.kmkbe.core.domain.model;

import com.kmkbe.core.domain.dto.PostedInvoiceDto;
import lombok.*;

import java.util.ArrayList;
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

    public static <T> PaginationResult<T> empty(
            int currentPage
    ) {
        return PaginationResult.<T>builder()
                .currentPage(currentPage)
                .totalData(0L)
                .totalPage(1)
                .list(new ArrayList<>())
                .build();
    }
}
