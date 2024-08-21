package com.kmkbe.core.factory;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class SimpleAuthenticationManager implements AuthenticationManager {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
       var a = authentication.getPrincipal();
        /*if (principal.getApikey() == null || principal.getTimestamp() == null || principal.getSignature() == null) {
            throw new BadCredentialsException("The API_KEY/timestamp/signature Header is missing.");
        }
        if (!apiKeysDatabase.isValidKey(principal.getApikey())) {
            throw new BadCredentialsException("The API key was not found or not the expected value.");
        }
        if (!tokenVerifier.isTokenAuthorized(principal)) {
            throw new BadCredentialsException("The signature is invalid");
        }*/
        authentication.setAuthenticated(true);
        return authentication;
    }
}
