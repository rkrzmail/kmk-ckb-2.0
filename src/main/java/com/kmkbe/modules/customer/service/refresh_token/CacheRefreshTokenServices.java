package com.kmkbe.modules.customer.service.refresh_token;

import com.kmkbe.core.utils.RedisUtils;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service("CacheRefreshTokenServices")
@RequiredArgsConstructor
@Slf4j
public class CacheRefreshTokenServices implements IRefreshTokenServices {
    private final RedisUtils<RefreshToken> redisUtil;

    @Override
    public RefreshToken create(Customer customer) {
        try {
            final RefreshToken payload = defaultPayload(customer);
            redisUtil.putValue(payload.getRefreshToken().toString(), payload, TimeUnit.DAYS.toMillis(2));
            return payload;
        } catch (Exception e) {
            log.error("Cache create, error {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Boolean invalidate(String refreshToken) {
        try {
            return redisUtil.deleteKey(refreshToken);
        } catch (Exception e) {
            log.error("Cache invalidate, error {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public RefreshToken verify(String refreshToken) throws IllegalAccessException {
        try {
            Object value = redisUtil.getValue(refreshToken);
            if (value instanceof Map<?, ?>) {
                //noinspection unchecked
                Map<String, Object> obj = (Map<String, Object>) value;

                RefreshToken token = new RefreshToken();
                token.setCustCode(UUID.fromString(obj.get("custCode").toString()));
                token.setRefreshToken(UUID.fromString(obj.get("refreshToken").toString()));
                token.setExpiredDate(new Date((Long) obj.get("expiredDate")));
                token.setIssuedDate(new Date((Long) obj.get("issuedDate")));

                if (token.getExpiredDate().before(new Date())) {
                    throw new IllegalStateException("Refresh token are expired or invalid, try to login again");
                }

                return token;
            }

            throw new RuntimeException("Refresh token not found");
        } catch (Exception e) {
            log.error("Cache verify, error {}", e.getMessage());
            throw e;
        }
    }
}
