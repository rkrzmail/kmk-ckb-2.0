package com.kmkbe.modules.internal.dto;

import lombok.*;

@Getter
@Setter
@Data
@Builder
public class InternalMailDto {
    private final String serverUrl;
    private final Integer port;
    private final String username;
    private final String password;
    private final Boolean enableSSL;
}
