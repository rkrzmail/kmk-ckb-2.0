package com.kmkbe.modules.remote.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class ExistingCustomerRequest {
    @JsonProperty("KeyAndValueObj")
    private Args args;

    @JsonProperty("IncludeProperties")
    private List<String> includeProperties;

    @JsonProperty("RequestDateTime")
    private String requestDateTime;

   /* @JsonProperty("Random")
    private String random;*/

    @Getter
    @Builder
    public static class Args {
        /**
         * <p>Example Key = IdNo</p>
         */
        @JsonProperty("Key")
        private KeyType key;

        /**
         * <p>Example Operator = EQ</p>
         */
        @Builder.Default
        @JsonProperty("Operator")
        private String operator = "EQ";

        /**
         * <p>Example Value = xxxxxx</p>
         */
        @JsonProperty("Value")
        private String value;


    }


    public enum KeyType{
        ktp("IdNo"),
        npwp("TaxIdNo");

        @JsonValue
        private final String value;

        KeyType(String value) {
            this.value = value;
        }
    }
}
