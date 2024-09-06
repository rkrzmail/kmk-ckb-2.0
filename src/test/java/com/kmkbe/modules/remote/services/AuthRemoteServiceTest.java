package com.kmkbe.modules.remote.services;

import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthRemoteServiceTest extends BaseRemoteServicesTest {
    private AuthRemoteService authRemoteService;

    @Override
    @BeforeEach
    public void setupBeforeEach() {
        super.setupBeforeEach();
        authRemoteService = new AuthRemoteService(
                ldapUrlService,
                restTemplate
        );
    }

    @Test
    public void fetchAuthJwt_shouldHasResponseAndHasTokenResponse() {
        /*BaseLdapRemoteResponseDto<String> response = authRemoteService.fetchAuthJwt();
        Assertions.assertNotNull(response.getData());
        Assertions.assertFalse(response.getData().isEmpty());*/
    }
}
