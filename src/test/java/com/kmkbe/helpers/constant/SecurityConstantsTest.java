package com.kmkbe.helpers.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConstantsTest {

  @Test
  void authenticationClaimEnum_values() {
    assertThat(SecurityConstants.AuthenticationClaim.values()).contains(
      SecurityConstants.AuthenticationClaim.REGISTRATION,
      SecurityConstants.AuthenticationClaim.LOGIN,
      SecurityConstants.AuthenticationClaim.RESET_PIN,
      SecurityConstants.AuthenticationClaim.EKYC,
      SecurityConstants.AuthenticationClaim.SYSTEM_SERVICE
    );
    assertThat(SecurityConstants.AuthenticationClaim.valueOf("LOGIN")).isEqualTo(SecurityConstants.AuthenticationClaim.LOGIN);
  }

  @Test
  void platformEnum_values() {
    assertThat(SecurityConstants.Platform.values()).contains(
      SecurityConstants.Platform.IOS,
      SecurityConstants.Platform.ANDROID,
      SecurityConstants.Platform.WEB
    );
    assertThat(SecurityConstants.Platform.valueOf("WEB")).isEqualTo(SecurityConstants.Platform.WEB);
  }

  @Test
  void tokenTypeEnum_values() {
    assertThat(SecurityConstants.TokenType.values()).contains(
      SecurityConstants.TokenType.REFRESH,
      SecurityConstants.TokenType.ACCESS,
      SecurityConstants.TokenType.TEMP
    );
    assertThat(SecurityConstants.TokenType.valueOf("ACCESS")).isEqualTo(SecurityConstants.TokenType.ACCESS);
  }

  @Test
  void constants_values() {
    assertThat(SecurityConstants.TOKEN_TYPE_CLAIM_KEY).isEqualTo("type");
    assertThat(SecurityConstants.TOKEN_ROLES_CLAIM_KEY).isEqualTo("roles");
    assertThat(SecurityConstants.TOKEN_USER_ID_CLAIM_KEY).isEqualTo("userId");
    assertThat(SecurityConstants.TOKEN_EMAIL_CLAIM_KEY).isEqualTo("email");
    assertThat(SecurityConstants.TOKEN_DEVICE_ID_CLAIM_KEY).isEqualTo("deviceId");
    assertThat(SecurityConstants.BEARER_TOKEN_PREFIX).isEqualTo("Bearer");
    assertThat(SecurityConstants.AUTHORIZATION_HEADER_KEY).isEqualTo("Authorization");
    assertThat(SecurityConstants.API_SECRET_HEADER_KEY).isEqualTo("api-secret");
    assertThat(SecurityConstants.JWT_TOKEN_VALIDITY).isEqualTo(720 * 60 * 60L);
    assertThat(SecurityConstants.AUTHORITIES).isEqualTo("authorities");
  }
}
