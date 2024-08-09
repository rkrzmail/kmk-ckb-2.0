package com.kmkbe.modules.remote.dto;

import lombok.*;

@Getter
@Setter
@Data
@Builder
public class MailRemoteDto {
    private final String serverUrl;
    private final Integer port;
    private final String username;
    private final String password;
    private final Boolean enableSSL;
}
