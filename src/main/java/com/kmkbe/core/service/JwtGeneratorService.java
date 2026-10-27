package com.kmkbe.core.service;

import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtGeneratorService {

  private final ApiSbuRepository apiSbuRepository;
  private final Clock clock;

  public JwtGeneratorService(ApiSbuRepository apiSbuRepository, Clock clock) {
    this.apiSbuRepository = apiSbuRepository;
    this.clock = clock;
  }

  /**
   * Generate JWT token
   *
   * @param appSecret     - dari header "ApiKey", untuk lookup secret di DB
   * @param bouwheer   - identitas user/tenant (masuk ke payload claims)
   * @param expireDate - durasi token valid (contoh: 3600 = 1 jam)
   * @return JWT string siap pakai
   */
  public String generateToken(String appSecret, String bouwheer, Date expireDate) {
    Date now = Date.from(clock.instant());
    return Jwts.builder()
      .header().add("typ", "JWT").and()
      .claim("bouwheer_code", bouwheer)
      .issuedAt(now)
      .expiration(expireDate)
      .signWith(
        Keys.hmacShaKeyFor(appSecret.getBytes(StandardCharsets.UTF_8)),
        Jwts.SIG.HS256
      )
      .compact();
  }

  public String generateToken(String apiKey, String bouwheer, long expireInSeconds) {
    Optional<ApiSbu> appSbuOpt = apiSbuRepository.findByAppKey(apiKey);
    if (appSbuOpt.isEmpty()) {
      throw new IllegalArgumentException("ApiKey tidak ditemukan / tidak valid");
    }

    String appSecret = appSbuOpt.get().getAppSecret();
    Instant nowInstant = clock.instant();
    Date now = Date.from(nowInstant);
    Date expireDate = Date.from(nowInstant.plusSeconds(expireInSeconds));

    return Jwts.builder()
      .header().add("typ", "JWT").and()
      .claim("bouwheer_code", bouwheer)
      .issuedAt(now)
      .expiration(expireDate)
      .signWith(
        Keys.hmacShaKeyFor(appSecret.getBytes(StandardCharsets.UTF_8)),
        Jwts.SIG.HS256
      )
      .compact();
  }
}
