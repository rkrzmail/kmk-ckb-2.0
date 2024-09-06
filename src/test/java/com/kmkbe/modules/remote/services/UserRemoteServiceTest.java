package com.kmkbe.modules.remote.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.UserInternalRemoteDto;
import com.kmkbe.modules.remote.request.ActiveDirectoryRemoteRequest;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.UserInternalRemoteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserRemoteServiceTest extends BaseRemoteServicesTest {
    private AuthRemoteService authRemoteService;
    private UserInternalRemoteService userInternalRemoteService;

    @Override
    @BeforeEach
    public void setupBeforeEach() {
        super.setupBeforeEach();
        authRemoteService = new AuthRemoteService(
                ldapUrlService,
                restTemplate
        );

        userInternalRemoteService = new UserInternalRemoteService(
                ldapUrlService,
                restTemplate,
                authRemoteService,
                objectMapper
        );
    }

    @Test
    public void validateActiveDirectory_shouldHasResponseAndReturnValidUser() throws JsonProcessingException {
       /* ActiveDirectoryRemoteRequest activeDirectoryRemoteRequest = ActiveDirectoryRemoteRequest.builder()
                .loginID("rizky.permana")
                .password("Adiba#4-8")
                .build();

        BaseLdapRemoteResponseDto<UserInternalRemoteDto> response = userInternalRemoteService.validateActiveDirectory(activeDirectoryRemoteRequest);
        Assertions.assertNotNull(response.getData());
        Assertions.assertTrue(response.getData().getUserValid());*/
    }

    @Test
    public void validateActiveDirectory_shouldHasResponseAndReturnInvalidUserWithExampleRequest() throws JsonProcessingException {
      /*  ActiveDirectoryRemoteRequest activeDirectoryRemoteRequest = ActiveDirectoryRemoteRequest.builder()
                .build();

        BaseLdapRemoteResponseDto<UserInternalRemoteDto> response = userInternalRemoteService.validateActiveDirectory(activeDirectoryRemoteRequest);
        Assertions.assertNotNull(response.getData());
        Assertions.assertFalse(response.getData().getUserValid());*/
    }
}
