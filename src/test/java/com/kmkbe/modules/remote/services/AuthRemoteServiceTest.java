package com.kmkbe.modules.remote.services;

import com.kmkbe.modules.remote.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.UserInternalRemoteServices;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthRemoteServiceTest extends BaseRemoteServicesTest {
    private AuthRemoteService authRemoteService;
    private UserInternalRemoteServices userInternalRemoteServices;

    @Override
    @BeforeEach
    public void setupBeforeEach() {
        super.setupBeforeEach();
        authRemoteService = new AuthRemoteService(
                ldapUrlService,
                restTemplate
        );

        userInternalRemoteServices = new UserInternalRemoteServices(
                ldapUrlService,
                restTemplate,
                authRemoteService,
                objectMapper
        );
    }

    @Test
    public void fetchAuthJwt_shouldHasResponseAndHasTokenResponse() {
        BaseLdapRemoteResponseDto<String> response = authRemoteService.fetchAuthJwt();
        Assertions.assertNotNull(response.getData());
        Assertions.assertFalse(response.getData().isEmpty());
    }
}
