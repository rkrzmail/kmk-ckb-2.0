package com.kmkbe.modules.remote.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

@Getter
@SuperBuilder
public class CriteriaGenericTypeRemoteRequest<T> {
    @Builder.Default
    @JsonProperty("includeCount")
    private Boolean includeCount = true;

    @Builder.Default
    @JsonProperty("includeData")
    private Boolean includeData = true;

    @Builder.Default
    @JsonProperty("isLoading")
    private Boolean isLoading = true;

    @JsonProperty("queryString")
    private QueryString queryString;

    @Builder.Default
    @JsonProperty("rowVersion")
    private String rowVersion = "";

    @JsonProperty("integrationObj")
    private Object integrationObj;

    @Builder.Default
    @JsonProperty("joinType")
    private String joinType = "INNER";

    @Builder.Default
    @JsonProperty("pageNo")
    private Integer pageNo = 1;

    @Builder.Default
    @JsonProperty("rowPerPage")
    private Integer rowPerPage = 10;

    @JsonProperty("orderBy")
    private Object orderBy;

    @JsonProperty("criteria")
    private List<T> criteria;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("RequestDateTime")
    private Date requestDateTime = new Date();

    @Builder
    @Getter
    public static class QueryString {
        @JsonProperty("name")
        private String name;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> whereQuery;

        public static QueryString zipCode() {
            return QueryString.builder().name("lookupZipcode").build();
        }

        public static QueryString inquiryCwr() {
            return QueryString.builder()
                    .name("searhCwrInquiry")
                    .whereQuery(List.of("FACTORING"))
                    .build();
        }

        public static QueryString inquiryAgreement() {
            return QueryString.builder()
                    .name("SearchAppDataInquiry")
                    .whereQuery(List.of("FCTR"))
                    .build();
        }
    }
}
