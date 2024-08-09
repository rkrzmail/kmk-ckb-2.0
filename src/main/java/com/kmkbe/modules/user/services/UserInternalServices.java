package com.kmkbe.modules.user.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.exception.AuthenticationException;
import com.kmkbe.modules.remote.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.modules.remote.dto.UserInternalRemoteDto;
import com.kmkbe.modules.remote.request.ActiveDirectoryRequest;
import com.kmkbe.modules.remote.service.UserInternalRemoteServices;
import com.kmkbe.modules.user.dto.UserDto;
import com.kmkbe.modules.user.dto.UserRoleFormDto;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.*;
import com.kmkbe.modules.user.request.LoginInternalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInternalServices {
    private final BCryptPasswordEncoder bcryptEncoder;
    private final MstUserRepository mstUserRepository;
    private final MstEmployeeRepository mstEmployeeRepository;
    private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
    private final UserInternalRemoteServices userInternalRemoteServices;


    public UserDto signIn(LoginInternalRequest request) throws JsonProcessingException {
        try {
            MstUser user;
            Optional<MstUser> findUser = mstUserRepository.findByUsername(request.getEmailOrUsername());
            if (findUser.isEmpty()) {
                MstEmployee findEmployee = mstEmployeeRepository
                        .findByEmail(request.getEmailOrUsername())
                        .orElseThrow(AuthenticationException::invalidInternalUser);

                user = mstUserRepository
                        .findByEmployeeCode(findEmployee)
                        .orElseThrow(AuthenticationException::invalidInternalUser);
            } else {
                user = findUser.get();
            }

            /*if (!user.getPassword().equals(bcryptEncoder.encode(request.getPassword()))) {
                throw new RuntimeException("Check your email or username or password");
            }

            if (user.getIsUserNonad()) {

            }

            if (user.getIsUserAd()) {

            }*/

            BaseLdapRemoteResponseDto<UserInternalRemoteDto> userResponse = userInternalRemoteServices.validateActiveDirectory(
                    ActiveDirectoryRequest.builder()
                            .loginID(user.getUsername())
                            .password(request.getPassword())
                            .build()
            );

            if (userResponse.getData().getUserValid()) {
                MstEmployee employee = user.getEmployeeCode();

                Optional<MstAppRoleFormUser> findPermission = mstAppRoleFormUserRepository.findTopByUserCodeOrderByAppRoleFormUserId(user);
                MstAppRoleFormUser permission = findPermission.orElseGet(() -> MstAppRoleFormUser.builder().build());

                List<UserRoleFormDto> roles = user.getAppRoleFormsUser()
                        .stream()
                        .map((role) -> UserRoleFormDto.builder()
                                .parentCode(role.getAppRoleFormCode().getFormCode().getParentCode())
                                .path(role.getAppRoleFormCode().getFormCode().getParentCode())
                                .icon(role.getAppRoleFormCode().getFormCode().getFormIcon())
                                .name(role.getAppRoleFormCode().getFormCode().getFormName())
                                .build())
                        .toList();

                return UserDto.builder()
                        .employeeName(employee.getEmployeeName())
                        .roleCode(
                                permission
                                        .getAppRoleFormCode()
                                        .getApplicationRoleCode()
                                        .getRoleCode()
                                        .getRoleCode()
                        )
                        .isActive(user.getIsActive())
                        .permissions(roles)
                        .build();
            }

            throw AuthenticationException.invalidInternalUser();
        } catch (Exception e) {
            log.error("signIn, error {}", e.getMessage());
            throw e;
        }
    }
}
