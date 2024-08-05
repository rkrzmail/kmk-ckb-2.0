package com.kmkbe.modules.customer.service.refresh_token;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.model.RefreshToken;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface IRefreshTokenServices {
    RefreshToken create(Customer customer);

    Boolean invalidate(String refreshToken);

    RefreshToken verify(String refreshToken) throws IllegalAccessException;

    default RefreshToken defaultPayload(Customer customer) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setCustCode(customer.getCustCode());
        refreshToken.setRefreshToken(UUID.randomUUID());
        refreshToken.setExpiredDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)));
        refreshToken.setIssuedDate(new Date());
        return refreshToken;
    }
}
