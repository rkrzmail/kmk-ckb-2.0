package com.kmkbe.core.middleware;

import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.service.ApiKeyAuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

public class ApiKeyAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    /*@Value("${security.api.key}")
    private String apiKey;*/

    private static final String API_KEY_HEADER = "ApiKey";

    /*public ApiKeyAuthenticationFilter() {
        super(new RequestHeaderRequestMatcher("ApiKey"));
    }*/

    public ApiKeyAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    public ApiKeyAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    public ApiKeyAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager) {
        super(defaultFilterProcessesUrl, authenticationManager);
    }

    public ApiKeyAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher, AuthenticationManager authenticationManager) {
        super(requiresAuthenticationRequestMatcher, authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String provideApiKey = request.getHeader(API_KEY_HEADER);

        if (provideApiKey == null) {
            throw new IllegalApiKeyException();
        }

       /* if (!provideApiKey.equals(apiKey)) {
            throw new IllegalApiKeyException();
        }*/

        Authentication auth = new ApiKeyAuthenticationService(provideApiKey);
        //auth.setAuthenticated(true);
        return getAuthenticationManager().authenticate(auth);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
