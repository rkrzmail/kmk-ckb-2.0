package com.kmkbe.core.service;

import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoreJwtServiceExtraTest {
  private static final String SECRET = "01234567890123456789012345678901";
  private static final Clock CLOCK = Clock.fixed(Instant.parse("2099-08-12T03:00:00Z"), ZoneId.of("Asia/Jakarta"));

  @Test void jwtServiceAllMethods() {
    JwtService service = new JwtService(mock(), mock(), CLOCK);
    ReflectionTestUtils.setField(service, "secretKey", Base64.getEncoder().encodeToString(SECRET.getBytes(StandardCharsets.UTF_8)));
    ReflectionTestUtils.setField(service, "jwtExpiration", 3_600_000L);
    ReflectionTestUtils.setField(service, "refreshExpirationDateInMs", 7_200_000);

    UserDetails user = User.withUsername("u").password("p").authorities("ROLE_USER").build();
    String token = service.generateToken(Map.of("extra","v"), user);
    assertThat(service.extractUsername(token)).isEqualTo("u");
    Function<Claims, String> resolver = c -> c.get("extra", String.class);
    assertThat(service.extractToken(token, resolver)).isEqualTo("v");
    assertThat(service.getExpirationTime()).isEqualTo(3_600_000L);
    assertThat(service.getRefreshTokenExpirationTime()).isEqualTo(7_200_000L);
    assertThat(service.generateToken(user)).isNotBlank();
    assertThat(service.generateRefreshToken(user)).isNotBlank();
    assertThat(service.isTokenValid(token, user)).isTrue();
    assertThat(service.isTokenValid(token, User.withUsername("other").password("p").authorities("ROLE_USER").build())).isFalse();

    JwtService later = new JwtService(mock(), mock(), Clock.fixed(Instant.parse("2100-01-01T00:00:00Z"), ZoneId.of("UTC")));
    ReflectionTestUtils.setField(later, "secretKey", Base64.getEncoder().encodeToString(SECRET.getBytes(StandardCharsets.UTF_8)));
    assertThat(later.isTokenValid(token, user)).isFalse();

    JwtEncoder encoder = mock(JwtEncoder.class);
    when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(Jwt.withTokenValue("oauth-token")
      .header("alg","none").claim("sub","u").build());
    JwtService oauth = new JwtService(mock(), encoder, CLOCK);
    var auth = new UsernamePasswordAuthenticationToken("u","p");
    assertThat(oauth.generateOauth2Token(auth)).isEqualTo("oauth-token");
  }

  @Test void jwtGeneratorServiceDirectAndMissingApiKey() {
    JwtGeneratorService service = new JwtGeneratorService(mock(ApiSbuRepository.class), CLOCK);
    String token = service.generateToken(SECRET, "bwh", Date.from(CLOCK.instant().plusSeconds(60)));
    assertThat(token).isNotBlank();

    ApiSbuRepository repo = mock(ApiSbuRepository.class);
    JwtGeneratorService missing = new JwtGeneratorService(repo, CLOCK);
    when(repo.findByAppKey("bad")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> missing.generateToken("bad","bwh",60))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("tidak ditemukan");
  }

  @Test void jwtValidatorServiceBranches() {
    ApiSbuRepository repo = mock(ApiSbuRepository.class);
    JwtValidatorService service = new JwtValidatorService(repo, CLOCK);
    when(repo.findByAppKey("bad")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.validate("bad","a.b.c"))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("tidak ditemukan");

    ApiSbu inactive = ApiSbu.builder().appSecret(SECRET).sesStatus("INACTIVE").build();
    when(repo.findByAppKey("inactive")).thenReturn(Optional.of(inactive));
    assertThatThrownBy(() -> service.validate("inactive","a.b.c"))
      .isInstanceOf(BusinessException.class);

    ApiSbu active = ApiSbu.builder().appSecret(SECRET).sesStatus("ACTIVE").build();
    assertThatThrownBy(() -> service.validate("ok","a.b", active))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Format JWT");

    String valid = token("bwh", CLOCK.instant().minusSeconds(10), CLOCK.instant().plusSeconds(60));
    ValidationResponse res = service.validate("ok", valid, active);
    assertThat(res.getBouwheer()).isEqualTo("bwh");
    assertThat(res.getApiKey()).isEqualTo("ok");

    String expired = token("bwh", CLOCK.instant().minusSeconds(120), CLOCK.instant().minusSeconds(60));
    assertThatThrownBy(() -> service.validate("ok", expired, active))
      .isInstanceOf(ExpiredJwtException.class);

    String noExp = Jwts.builder().claim("bouwheer_code","bwh")
      .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
    assertThat(service.validate("ok", noExp, active).getExp()).isNull();

    String wrong = Jwts.builder().claim("bouwheer_code","bwh")
      .signWith(Keys.hmacShaKeyFor("99999999999999999999999999999999".getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
    assertThatThrownBy(() -> service.validate("ok", wrong, active)).isInstanceOf(JwtException.class);
  }

  private String token(String bouwheer, Instant iat, Instant exp) {
    return Jwts.builder().claim("bouwheer_code", bouwheer)
      .issuedAt(Date.from(iat)).expiration(Date.from(exp))
      .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
  }
}
