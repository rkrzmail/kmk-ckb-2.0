package com.kmkbe.modules.common.service.refresh_token;

import com.kmkbe.modules.common.model.RefreshToken;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface IRefreshTokenServices {
    RefreshToken create(IRefreshTokenServices.User customer);

    Boolean invalidate(String refreshToken);

    RefreshToken verify(String refreshToken) throws IllegalAccessException;

    default RefreshToken defaultPayload(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserCode(user.getUserCode());
        refreshToken.setRefreshToken(UUID.randomUUID());
        refreshToken.setExpiredDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)));
        refreshToken.setIssuedDate(new Date());
        return refreshToken;
    }

    @Builder
    @Getter
    class User {
        private UUID userCode;
    }
}
