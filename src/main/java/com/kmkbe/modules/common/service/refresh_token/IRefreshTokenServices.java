package com.kmkbe.modules.common.service.refresh_token;

import com.kmkbe.core.domain.model.RefreshToken;
import lombok.Builder;
import lombok.Getter;

import java.time.Clock;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface IRefreshTokenServices {
    RefreshToken create(IRefreshTokenServices.User customer);

    Boolean invalidate(String refreshToken);

    RefreshToken verify(String refreshToken) throws IllegalAccessException;

    default RefreshToken defaultPayload(User user) {
        return defaultPayload(user, Clock.systemDefaultZone());
    }

    default RefreshToken defaultPayload(User user, Clock clock) {
        RefreshToken refreshToken = new RefreshToken();
        Date issuedDate = Date.from(clock.instant());
        refreshToken.setUserCode(user.getUserCode());
        refreshToken.setRefreshToken(UUID.randomUUID());
        refreshToken.setExpiredDate(Date.from(issuedDate.toInstant().plusMillis(TimeUnit.DAYS.toMillis(1))));
        //refreshToken.setExpiredDate(Date.from(issuedDate.toInstant().plusMillis(TimeUnit.MINUTES.toMillis(5))));
        refreshToken.setIssuedDate(issuedDate);
        return refreshToken;
    }

    @Builder
    @Getter
    class User {
        private UUID userCode;
    }
}
