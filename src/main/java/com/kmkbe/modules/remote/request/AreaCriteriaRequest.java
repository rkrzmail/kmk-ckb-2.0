package com.kmkbe.modules.remote.request;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
public class AreaCriteriaRequest
        extends CriteriaGenericTypeRequest<CriteriaGenericTypeRequest<PropCriteriaGenericTypeRequest>> {
/*
    @Override
    public QueryString getQueryString() {
        return QueryString.builder()
                .name("lookupZipcode")
                .build();
    }*/

    /*@Override
    public List<CriteriaGenericTypeRequest<PropCriteriaGenericTypeRequest>> getCriteria() {
        return super.getCriteria().stream().map((e) -> CriteriaGenericTypeRequest.<PropCriteriaGenericTypeRequest>builder()
                .queryString(this.getQueryString())
                .includeCount(e.getIncludeCount())
                .includeData(e.getIncludeData())
                .isLoading(e.getIsLoading())
                .rowVersion(e.getRowVersion())
                .integrationObj(e.getIntegrationObj())
                .joinType(e.getJoinType())
                .pageNo(e.getPageNo())
                .rowPerPage(e.getRowPerPage())
                .orderBy(e.getOrderBy())
                .build()
        ).toList();
    }*/
}
