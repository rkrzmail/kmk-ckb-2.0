package com.kmkbe.modules.user;

import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.user.controller.AuthInternalController;
import com.kmkbe.modules.user.controller.UserInternalController;
import com.kmkbe.modules.user.dto.UserDto;
import com.kmkbe.modules.user.entity.MstAppRoleForm;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstApplicationRole;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstForm;
import com.kmkbe.modules.user.entity.MstRole;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.request.LoginInternalRequest;
import com.kmkbe.modules.user.service.AuthInternalServices;
import com.kmkbe.modules.user.service.UserInternalService;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.SignatureException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInternalRemainingCoverageTest {
  @Mock MstAppRoleFormUserRepository permissionRepository;
  @Mock MstUserRepository userRepository;
  @Mock AuthInternalServices authInternalServices;
  @Mock CurrentUserService currentUserService;

  @Test
  void userInternalServiceProfile() throws Exception {
    UserInternalService service = new UserInternalService(permissionRepository, userRepository);
    MstUser user = user();
    when(userRepository.findById(user.getUserCode())).thenReturn(Optional.of(user));
    when(permissionRepository.findTopByUserOrderByAppRoleFormUserId(user))
      .thenReturn(Optional.of(permission()));

    UserDto profile = service.profile(user);
    assertThat(profile.getEmployeeName()).isEqualTo("Employee");
    assertThat(profile.getRoleCode()).isEqualTo("role");
    assertThat(profile.getPermissions()).hasSize(1);

    when(userRepository.findById(user.getUserCode())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.profile(user)).isInstanceOf(Exception.class);
  }

  @Test
  void userInternalControllers() throws Exception {
    AuthInternalController authController = new AuthInternalController(authInternalServices);
    LoginDto login = new LoginDto();
    when(authInternalServices.signIn(any(LoginInternalRequest.class))).thenReturn(login);
    when(authInternalServices.refreshToken(any(RefreshTokenRequest.class))).thenReturn(login);
    CommonResult<LoginDto> signedIn = authController.signIn(new LoginInternalRequest());
    assertThat(signedIn.isSuccess()).isTrue();
    assertThat(authController.refreshToken(new RefreshTokenRequest("token")).isSuccess()).isTrue();

    UserInternalService userInternalService = mock(UserInternalService.class);
    UserInternalController controller = new UserInternalController(userInternalService, currentUserService);
    MstUser user = user();
    when(currentUserService.internalUser()).thenReturn(user);
    when(userInternalService.profile(user)).thenReturn(UserDto.builder().employeeName("Employee").build());
    assertThat(controller.getProfile().isSuccess()).isTrue();
  }

  @Test
  void userInternalUtilsBranches() throws Exception {
    MstUser user = user();
    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
    assertThat(UserInternalUtils.authenticateUser(authentication)).isSameAs(user);
    UserInternalUtils.authenticated(authentication);

    Authentication invalid = new UsernamePasswordAuthenticationToken("principal", null, List.of());
    assertThatThrownBy(() -> UserInternalUtils.authenticateUser(invalid)).isInstanceOf(RuntimeException.class);

    Authentication anonymous = new AnonymousAuthenticationToken("key", "anonymous",
      List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    assertThatThrownBy(() -> UserInternalUtils.authenticated(anonymous)).isInstanceOf(SignatureException.class);
  }

  private static MstUser user() {
    MstAppRoleForm appRoleForm = MstAppRoleForm.builder()
      .form(MstForm.builder().formCode("form").formName("Form").parentCode("parent").formIcon("icon").build())
      .applicationRole(MstApplicationRole.builder()
        .roleCode(MstRole.builder().roleCode("role").build()).build())
      .build();
    MstAppRoleFormUser permission = MstAppRoleFormUser.builder().appRoleForm(appRoleForm).isActive(true).build();
    return MstUser.builder()
      .userCode(UUID.randomUUID())
      .username("u")
      .isActive(true)
      .employee(MstEmployee.builder()
        .employeeName("Employee")
        .branch(MstBranch.builder().branchCode("412").branchName("Jakarta").build())
        .build())
      .appRoleFormsUser(Set.of(permission))
      .build();
  }

  private static MstAppRoleFormUser permission() {
    return MstAppRoleFormUser.builder()
      .appRoleForm(MstAppRoleForm.builder()
        .applicationRole(MstApplicationRole.builder()
          .roleCode(MstRole.builder().roleCode("role").build()).build())
        .form(MstForm.builder().formCode("form").build())
        .build())
      .isActive(true)
      .build();
  }
}
