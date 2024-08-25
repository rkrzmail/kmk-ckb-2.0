package com.kmkbe.modules.loan_submission.request;

import com.kmkbe.core.domain.model.PaginationPayload;
import com.kmkbe.core.domain.constant.ProductSearchBy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProductListRequest extends PaginationPayload {
    private ProductSearchBy searchBy;
    private String searchByValue;
}
