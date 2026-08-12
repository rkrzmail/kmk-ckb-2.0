package com.kmkbe.core.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtServiceTest {

    @Test
    void generateTokenUsesFixedClockForIssuedAtAndExpiration() {
        Clock clock = Clock.fixed(Instant.parse("2099-08-12T03:00:00Z"), ZoneId.of("Asia/Jakarta"));
        JwtService service = new JwtService(
                mock(org.springframework.security.oauth2.jwt.JwtDecoder.class),
                mock(org.springframework.security.oauth2.jwt.JwtEncoder.class),
                clock
        );
        ReflectionTestUtils.setField(service, "secretKey", base64Secret());
        ReflectionTestUtils.setField(service, "jwtExpiration", 3_600_000L);

        UserDetails user = User.withUsername("user@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        String token = service.generateToken(user);
        Claims claims = service.extractAllClaims(token);

        assertThat(claims.getSubject()).isEqualTo("user@example.com");
        assertThat(claims.getIssuer()).isEqualTo("user@example.com@DanaSakti");
        assertThat(claims.getIssuedAt()).isEqualTo(Date.from(clock.instant()));
        assertThat(claims.getExpiration()).isEqualTo(Date.from(clock.instant().plusMillis(3_600_000L)));
        assertThat(service.isTokenValid(token, user)).isTrue();
    }

    private String base64Secret() {
        return Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes());
    }
}
