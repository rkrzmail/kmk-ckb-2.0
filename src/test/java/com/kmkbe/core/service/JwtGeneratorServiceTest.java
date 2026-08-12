package com.kmkbe.core.service;

import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtGeneratorServiceTest {

    @Test
    void generateTokenLooksUpSecretAndUsesClockForExpiry() {
        ApiSbuRepository repository = mock(ApiSbuRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2099-08-12T03:00:00Z"), ZoneId.of("Asia/Jakarta"));
        JwtGeneratorService service = new JwtGeneratorService(repository, clock);
        String secret = "01234567890123456789012345678901";
        String apiKey = "api-key";
        UUID bouwheer = UUID.randomUUID();

        when(repository.findByAppKey(apiKey)).thenReturn(Optional.of(ApiSbu.builder()
                .appSecret(secret)
                .build()));

        String token = service.generateToken(apiKey, bouwheer.toString(), 600);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.get("bouwheer_code", String.class)).isEqualTo(bouwheer.toString());
        assertThat(claims.getIssuedAt()).isEqualTo(Date.from(clock.instant()));
        assertThat(claims.getExpiration()).isEqualTo(Date.from(clock.instant().plusSeconds(600)));
    }
}
