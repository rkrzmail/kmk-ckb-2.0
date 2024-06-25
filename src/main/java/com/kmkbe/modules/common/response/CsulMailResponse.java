package com.kmkbe.modules.common.response;

import lombok.*;

@Getter
@Setter
@Data
@Builder
public class CsulMailResponse {
    private final String serverUrl;
    private final Integer port;
    private final String username;
    private final String password;
    private final Boolean enableSSL;
}
