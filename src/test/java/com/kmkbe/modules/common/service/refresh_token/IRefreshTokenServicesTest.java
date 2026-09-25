package com.kmkbe.modules.common.service.refresh_token;

import com.kmkbe.core.domain.model.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IRefreshTokenServicesTest {

    private final IRefreshTokenServices service = new IRefreshTokenServices() {
        @Override
        public RefreshToken create(User customer) {
            return null;
        }

        @Override
        public Boolean invalidate(String refreshToken) {
            return null;
        }

        @Override
        public RefreshToken verify(String refreshToken) {
            return null;
        }
    };

    @Test
    void defaultPayloadUsesProvidedClockForIssuedAndExpiredDate() {
        UUID userCode = UUID.randomUUID();
        Clock clock = Clock.fixed(Instant.parse("2026-08-12T03:00:00Z"), ZoneId.of("Asia/Jakarta"));

        RefreshToken payload = service.defaultPayload(
                IRefreshTokenServices.User.builder().userCode(userCode).build(),
                clock
        );

        assertThat(payload.getUserCode()).isEqualTo(userCode);
        assertThat(payload.getRefreshToken()).isNotNull();
        assertThat(payload.getIssuedDate()).isEqualTo(Date.from(clock.instant()));
        assertThat(payload.getExpiredDate()).isEqualTo(Date.from(clock.instant().plusSeconds(86_400)));
    }
}
