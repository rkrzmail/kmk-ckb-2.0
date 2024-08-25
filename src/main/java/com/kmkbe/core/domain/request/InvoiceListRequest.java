package com.kmkbe.core.domain.request;

import com.kmkbe.core.domain.model.PaginationPayload;
import com.kmkbe.core.domain.constant.InvoiceSearchBy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class InvoiceListRequest extends PaginationPayload {
    private SearchBy searchBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class SearchBy {
        private InvoiceSearchBy search;
        private String value;
    }
}
