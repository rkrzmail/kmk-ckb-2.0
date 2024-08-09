package com.kmkbe.modules.remote.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExistingCustomerRequest {
    @JsonProperty("KeyAndValueObj")
    private Args args;

    @JsonProperty("IncludeProperties")
    private List<String> includeProperties;

    @JsonProperty("RequestDateTime")
    private String requestDateTime;

    @Getter
    @Builder
    public static class Args {
        /**
         * <p>Example Key = IdNo</p>
         */
        @JsonProperty("Key")
        private String key;

        /**
         * <p>Example Operator = EQ</p>
         */
        @JsonProperty("Operator")
        private String operator;

        /**
         * <p>Example Value = xxxxxx</p>
         */
        @JsonProperty("Value")
        private String value;
    }
}
