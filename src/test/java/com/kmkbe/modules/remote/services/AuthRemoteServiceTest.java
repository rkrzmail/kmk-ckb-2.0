package com.kmkbe.modules.remote.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.modules.remote.dto.BaseRemoteResponseDto;
import com.kmkbe.modules.remote.dto.UserRemoteDto;
import com.kmkbe.modules.remote.request.ActiveDirectoryRequest;
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
                authRemoteService
        );
    }

    @Test
    public void fetchAuthJwt_shouldHasResponseAndHasTokenResponse() {
        BaseRemoteResponseDto<String> response = authRemoteService.fetchAuthJwt();
        Assertions.assertNotNull(response.getData());
        Assertions.assertFalse(response.getData().isEmpty());
    }

    @Test
    public void validateActiveDirectory_shouldHasResponseAndReturnInvalidUserWithExampleRequest() throws JsonProcessingException {
        ActiveDirectoryRequest activeDirectoryRequest = ActiveDirectoryRequest.builder()
                .build();

        BaseRemoteResponseDto<UserRemoteDto> response = userInternalRemoteServices.validateActiveDirectory(activeDirectoryRequest);
        Assertions.assertNotNull(response.getData());
        Assertions.assertFalse(response.getData().isUserValid());
    }
}
