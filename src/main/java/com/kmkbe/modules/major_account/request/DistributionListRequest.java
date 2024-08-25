package com.kmkbe.modules.major_account.request;

import com.kmkbe.core.domain.model.PaginationPayload;
import com.kmkbe.core.domain.constant.DistributionListSearchBy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DistributionListRequest extends PaginationPayload {
    private Date startDate = new Date();
    private Date endDate = new Date();
    private SearchBy searchBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class SearchBy {
        private DistributionListSearchBy search = DistributionListSearchBy.NamaDebitur;
        private String value;
    }
}
