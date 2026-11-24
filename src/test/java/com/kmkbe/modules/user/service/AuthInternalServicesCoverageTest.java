package com.kmkbe.modules.user.service;

import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.UserInternalRemoteDto;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.model.RefreshToken;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.remote.service.UserInternalRemoteService;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstEmployeeRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.request.LoginInternalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthInternalServicesCoverageTest {
  @Mock BCryptPasswordEncoder encoder;
  @Mock MstUserRepository userRepository;
  @Mock MstEmployeeRepository employeeRepository;
  @Mock UserInternalRemoteService userInternalRemoteService;
  @Mock RedisRepository redisRepository;
  @Mock RedisAttackRepository attackRepository;
  @Mock AuthenticationManager authenticationManager;
  @Mock JwtService jwtService;
  @Mock MstAppRoleFormUserRepository permissionRepository;
  @Mock AuditTrailService auditTrailService;
  @Mock IRefreshTokenServices refreshTokenServices;
  AuthInternalServices service;

  @BeforeEach
  void setUp() {
    service = new AuthInternalServices(encoder, userRepository, employeeRepository, userInternalRemoteService,
      redisRepository, attackRepository, authenticationManager, jwtService, permissionRepository, auditTrailService);
    ReflectionTestUtils.setField(service, "refreshTokenServices", refreshTokenServices);
    when(attackRepository.findTopByRedis(anyString())).thenReturn(Optional.empty());
    when(attackRepository.save(any(RedisAttack.class))).thenAnswer(call -> call.getArgument(0));
    when(encoder.matches("pw", "hash")).thenReturn(true);
    when(jwtService.generateToken(any())).thenReturn("jwt");
    when(jwtService.getExpirationTime()).thenReturn(1000L);
    RefreshToken token = new RefreshToken();
    token.setRefreshToken(UUID.randomUUID());
    when(refreshTokenServices.create(any())).thenReturn(token);
  }

  @Test
  void signInExistingNonAdSuccessAndPasswordFailures() throws Exception {
    MstUser user = user(false);
    when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
    when(permissionRepository.findTopByUserOrderByAppRoleFormUserId(user))
      .thenReturn(Optional.of(permission(true)));

    assertThat(service.signIn(request("u", "pw")).getToken()).isEqualTo("jwt");

    when(encoder.matches("bad", "hash")).thenReturn(false);
    assertThatThrownBy(() -> service.signIn(request("u", "bad"))).isInstanceOf(RuntimeException.class);
  }

  @Test
  void signInAttackBranches() throws Exception {
    MstUser user = user(false);
    when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
    when(permissionRepository.findTopByUserOrderByAppRoleFormUserId(user))
      .thenReturn(Optional.of(permission(true)));
    RedisAttack recent = attack(6, new Date());
    doReturn(Optional.of(recent)).when(attackRepository).findTopByRedis("InternalsignIn:u");
    assertThatThrownBy(() -> service.signIn(request("u", "pw"))).isInstanceOf(RuntimeException.class);

    RedisAttack old = attack(6, new Date(0));
    doReturn(Optional.of(old)).when(attackRepository).findTopByRedis("InternalsignIn:u");
    assertThat(service.signIn(request("u", "pw")).getToken()).isEqualTo("jwt");
  }

  @Test
  void signInAdBranches() throws Exception {
    MstUser user = user(true);
    user.setEmployee(MstEmployee.builder().employeeCode("E1").build());
    when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
    when(permissionRepository.findTopByUserOrderByAppRoleFormUserId(user))
      .thenReturn(Optional.of(permission(true)));
    BaseLdapRemoteResponseDto<UserInternalRemoteDto> response = new BaseLdapRemoteResponseDto<>();
    UserInternalRemoteDto data = new UserInternalRemoteDto();
    data.setUserValid(true);
    response.setData(data);
    when(userInternalRemoteService.validateActiveDirectory(any())).thenReturn(response);
    assertThat(service.signIn(request("u", "pw")).getToken()).isEqualTo("jwt");

    data.setUserValid(false);
    assertThatThrownBy(() -> service.signIn(request("u", "pw"))).isInstanceOf(RuntimeException.class);

    user.setEmployee(null);
    assertThatThrownBy(() -> service.signIn(request("u", "pw"))).isInstanceOf(RuntimeException.class);
  }

  @Test
  void signInEmployeeFallbackAndPermissionFailures() throws Exception {
    MstUser user = user(false);
    MstEmployee employee = MstEmployee.builder().employeeCode("E1").email("u").build();
    when(userRepository.findByUsername("u")).thenReturn(Optional.empty());
    when(employeeRepository.findByEmail("u")).thenReturn(Optional.of(employee));
    when(userRepository.findByEmployee(employee)).thenReturn(Optional.of(user));
    when(permissionRepository.findTopByUserOrderByAppRoleFormUserId(user))
      .thenReturn(Optional.of(permission(false)), Optional.empty());
    assertThatThrownBy(() -> service.signIn(request("u", "pw"))).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> service.signIn(request("u", "pw"))).isInstanceOf(RuntimeException.class);

    when(employeeRepository.findByEmail("u")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.signIn(request("u", "pw"))).isInstanceOf(RuntimeException.class);
  }

  @Test
  void refreshTokenBranches() throws Exception {
    RefreshToken payload = new RefreshToken();
    payload.setUserCode(UUID.randomUUID());
    payload.setRefreshToken(UUID.randomUUID());
    when(refreshTokenServices.verify("token")).thenReturn(payload);
    when(userRepository.findByUserCode(payload.getUserCode())).thenReturn(Optional.of(user(false)));
    assertThat(service.refreshToken(new RefreshTokenRequest("token")).getToken()).isEqualTo("jwt");

    when(userRepository.findByUserCode(payload.getUserCode())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.refreshToken(new RefreshTokenRequest("token")))
      .isInstanceOf(IllegalStateException.class);

    when(refreshTokenServices.verify("token")).thenThrow(new IllegalAccessException("boom"));
    assertThatThrownBy(() -> service.refreshToken(new RefreshTokenRequest("token")))
      .isInstanceOf(IllegalAccessException.class);
  }

  private static LoginInternalRequest request(String username, String password) {
    LoginInternalRequest request = new LoginInternalRequest();
    request.setUsername(username);
    request.setPassword(password);
    return request;
  }

  private static MstUser user(boolean isAd) {
    return MstUser.builder()
      .userCode(UUID.randomUUID())
      .username("u")
      .password("hash")
      .isUserAd(isAd)
      .isUserNonad(!isAd)
      .isActive(true)
      .build();
  }

  private static MstAppRoleFormUser permission(boolean active) {
    return MstAppRoleFormUser.builder().isActive(active).build();
  }

  private static RedisAttack attack(int count, Date modifiedDate) {
    return RedisAttack.builder().redis("InternalsignIn:u").countAttack(count).modifiedDate(modifiedDate).build();
  }
}
