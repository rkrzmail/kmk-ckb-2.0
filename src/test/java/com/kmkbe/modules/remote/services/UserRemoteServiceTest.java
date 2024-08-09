package com.kmkbe.modules.remote.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.modules.remote.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.modules.remote.dto.UserInternalRemoteDto;
import com.kmkbe.modules.remote.request.ActiveDirectoryRequest;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.UserInternalRemoteServices;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserRemoteServiceTest extends BaseRemoteServicesTest {
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
    public void validateActiveDirectory_shouldHasResponseAndReturnValidUser() throws JsonProcessingException {
        ActiveDirectoryRequest activeDirectoryRequest = ActiveDirectoryRequest.builder()
                .loginID("rizky.permana")
                .password("Adiba#4-8")
                .build();

        BaseLdapRemoteResponseDto<UserInternalRemoteDto> response = userInternalRemoteServices.validateActiveDirectory(activeDirectoryRequest);
        Assertions.assertNotNull(response.getData());
        Assertions.assertTrue(response.getData().getUserValid());
    }

    @Test
    public void validateActiveDirectory_shouldHasResponseAndReturnInvalidUserWithExampleRequest() throws JsonProcessingException {
        ActiveDirectoryRequest activeDirectoryRequest = ActiveDirectoryRequest.builder()
                .build();

        BaseLdapRemoteResponseDto<UserInternalRemoteDto> response = userInternalRemoteServices.validateActiveDirectory(activeDirectoryRequest);
        Assertions.assertNotNull(response.getData());
        Assertions.assertFalse(response.getData().getUserValid());
    }
}
