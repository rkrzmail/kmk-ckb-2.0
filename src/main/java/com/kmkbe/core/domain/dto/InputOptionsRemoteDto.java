package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class InputOptionsRemoteDto {
    @JsonProperty("key")
    @JsonAlias("Key")
    public String key;

    @JsonProperty("value")
    @JsonAlias("Value")
    public String value;
}
