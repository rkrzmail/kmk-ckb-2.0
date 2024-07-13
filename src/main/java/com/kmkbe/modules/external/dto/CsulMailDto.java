package com.kmkbe.modules.external.dto;

import lombok.*;

@Getter
@Setter
@Data
@Builder
public class CsulMailDto {
    private final String serverUrl;
    private final Integer port;
    private final String username;
    private final String password;
    private final Boolean enableSSL;
}
