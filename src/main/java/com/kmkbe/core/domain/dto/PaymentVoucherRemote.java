package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public final class PaymentVoucherRemote {
    private PaymentVoucherRemote() {
    }

    public record PagingRequest(
        boolean includeCount,
        boolean includeData,
        boolean isLoading,
        QueryString queryString,
        String rowVersion,
        Object integrationObj,
        String joinType,
        int pageNo,
        int rowPerPage,
        OrderBy orderBy,
        List<Criterion> criteria,
        @JsonProperty("RequestDateTime") String requestDateTime
    ) {
    }

    public record QueryString(String name) {
    }

    public record OrderBy(String key, String value) {
    }

    public record Criterion(
        int low,
        int high,
        @JsonProperty("DataType") String dataType,
        boolean isCriteriaDataTable,
        String restriction,
        String propName,
        String value
    ) {
    }

    public record DetailRequest(
        @JsonProperty("PayVoucherNo") String payVoucherNo,
        @JsonProperty("RequestDateTime") String requestDateTime
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PagingResponse(
        @JsonProperty("Data") List<PagingRow> data,
        @JsonProperty("Count") Integer count,
        @JsonProperty("HeaderObj") Header header,
        @JsonProperty("StatusCode") String statusCode
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PagingRow(
        @JsonProperty("APNo") String apNo,
        @JsonProperty("APNoArray") String apNoArray,
        @JsonProperty("PayVoucherNo") String payVoucherNo,
        @JsonProperty("PayVoucherStatCode") String statusCode
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(@JsonProperty("StatusCode") String statusCode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetailResponse(
        @JsonProperty("PayVoucherNo") String payVoucherNo,
        @JsonProperty("PayVoucherStatCode") String statusCode,
        @JsonProperty("ExecutionValueDt") LocalDateTime executionValueDt
    ) {
    }
}
