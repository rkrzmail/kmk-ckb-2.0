package com.kmkbe.core.service;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

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
        return apiKey;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}
