package com.kmkbe.modules.user.service;

import com.kmkbe.modules.user.dto.UserDto;
import com.kmkbe.modules.user.dto.UserRoleFormDto;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInternalService {
    private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
    private final MstUserRepository mstUserRepository;

    @Transactional
    public UserDto profile(Authentication authentication) throws SignatureException {
        try {
            MstUser authenticateUser = UserInternalUtils.authenticateUser(authentication);
            MstUser user = mstUserRepository.findById(authenticateUser.getUserCode()).orElseThrow();
            MstEmployee employee = user.getEmployee();

            Optional<MstAppRoleFormUser> findPermission = mstAppRoleFormUserRepository
                    .findTopByUserOrderByAppRoleFormUserId(user);
            MstAppRoleFormUser permission = findPermission
                    .orElseGet(() -> MstAppRoleFormUser.builder().build());

            List<UserRoleFormDto> roles = user.getAppRoleFormsUser()
                    .stream()
                    .map((role) -> UserRoleFormDto.builder()
                            .parentCode(role.getAppRoleForm().getForm().getParentCode())
                            .path(role.getAppRoleForm().getForm().getParentCode())
                            .icon(role.getAppRoleForm().getForm().getFormIcon())
                            .name(role.getAppRoleForm().getForm().getFormName())
                            .build())
                    .toList();

            return UserDto.builder()
                    .employeeName(employee.getEmployeeName())
                    .branchCode(employee.getBranch().getBranchCode())
                    .branchName(employee.getBranch().getBranchName())
                    .roleCode(
                            permission
                                    .getAppRoleForm()
                                    .getApplicationRole()
                                    .getRoleCode()
                                    .getRoleCode()
                    )
                    .isActive(user.getIsActive())
                    .permissions(roles)
                    .build();
        } catch (Exception e) {
            log.error("signIn, error {}", e.getMessage());
            throw e;
        }
    }
}
