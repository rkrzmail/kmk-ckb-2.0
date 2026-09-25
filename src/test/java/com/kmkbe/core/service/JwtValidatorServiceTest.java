package com.kmkbe.core.service;

import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtValidatorServiceTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void validateReturnsClaimsWhenTokenIsValid() {
        ApiSbuRepository repository = mock(ApiSbuRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2099-08-12T03:00:00Z"), ZoneId.of("Asia/Jakarta"));
        JwtValidatorService service = new JwtValidatorService(repository, clock);
        String apiKey = "api-key";
        String bouwheer = UUID.randomUUID().toString();
        ApiSbu apiSbu = ApiSbu.builder()
                .appSecret(SECRET)
                .sesStatus("ACTIVE")
                .build();

        when(repository.findByAppKey(apiKey)).thenReturn(Optional.of(apiSbu));

        ValidationResponse response = service.validate(apiKey, token(bouwheer, clock.instant(), clock.instant().plusSeconds(600)));

        assertThat(response.getBouwheer()).isEqualTo(bouwheer);
        assertThat(response.getApiKey()).isEqualTo(apiKey);
        assertThat(response.getExp()).isEqualTo(clock.instant().plusSeconds(600).toEpochMilli() / 1000);
    }

    @Test
    void validateUsesInjectedClockForManualExpiryCheck() {
        ApiSbuRepository repository = mock(ApiSbuRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2099-01-01T00:00:00Z"), ZoneId.of("Asia/Jakarta"));
        JwtValidatorService service = new JwtValidatorService(repository, clock);
        String apiKey = "api-key";
        ApiSbu apiSbu = ApiSbu.builder()
                .appSecret(SECRET)
                .sesStatus("ACTIVE")
                .build();

        when(repository.findByAppKey(apiKey)).thenReturn(Optional.of(apiSbu));

        assertThatThrownBy(() -> service.validate(
                apiKey,
                token(UUID.randomUUID().toString(), Instant.parse("2098-01-01T00:00:00Z"), Instant.parse("2098-12-31T00:00:00Z"))
        )).isInstanceOf(ExpiredJwtException.class);
    }

    private String token(String bouwheer, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .claim("bouwheer_code", bouwheer)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
