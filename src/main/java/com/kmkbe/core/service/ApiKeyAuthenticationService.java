package com.kmkbe.core.service;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;

public class ApiKeyAuthenticationService extends AbstractAuthenticationToken {
    private final String apiKey;

    public ApiKeyAuthenticationService(
            String apiKey
    ) {
        super(null);
        this.apiKey = apiKey;
        setAuthenticated(false);
    }

    public ApiKeyAuthenticationService(
            Collection<? extends GrantedAuthority> authorities,
            String apiKey
    ) {
        super(authorities);
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return getCredentials();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        return Objects.equals(apiKey, ((ApiKeyAuthenticationService) obj).apiKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), apiKey);
    }
}
