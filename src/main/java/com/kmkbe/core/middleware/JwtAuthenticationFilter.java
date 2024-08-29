package com.kmkbe.core.middleware;

import com.kmkbe.core.domain.model.JwtSimulasiModel;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${spring.profiles.active}")
    private String profileActives;

    /*@Value("${security.api.key}")
    private String apiKey;*/

    public static final String[] ENDPOINTS_WHITELIST_FINANCING = {
            "/api/v1/financing/invoice-paid"
    };

    public static final String[] ENDPOINTS_WHITELIST_LOAN_SUBMISSION = {
            "/api/v1/loan-submissions",
            "/api/v1/loan-submissions/invoices",
            "/api/v1/loan-submissions/importance-notes",
            "/api/v1/loan-submissions/simulations/percentage",
            "/api/v1/loan-submissions/simulations/calculate",
    };

    public static final String[] ENDPOINTS_WHITELIST = {
            "/api/v1/auth/sign-in",
            "/api/v1/auth/sign-up",
            "/api/v1/auth/forgot-pin",
            "/api/v1/auth/refresh-token",
            "/api/v1/otp/**",
            "/api/v1/error/**",
            "/api/v1/uploads/**",
            "/api/v1/testing/**",
            "/api/v1/documents/download/loan/**",

            // internal
            //"/api/v1/internal/user/**",
            "/api/v1/internal/auth/**",
            //"/api/v1/internal/auth/refresh-token",

            "/uploads/**",
            "/error/**",
    };

    public static final String[] ENDPOINTS_SWAGGERS = {
            "/api/v1/actuator/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/configuration/**",
            "/webjars/**",
            "/actuator/**",
            "/instances/**",
            "/actuator/**",

            "/api/v1/api-docs/**",
            "/api/v1/swagger-ui/**",
            "/api/v1/swagger-resources/**",
            "/api/v1/configuration/**",
            "/api/v1/webjars/**",
            "/api/v1/instances/**",
    };

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final JwtLoanSubmissionService jwtLoanSubmissionService;
    private final UserDetailsService userDetailsService;

    @Qualifier("internalUserDetailService")
    private final UserDetailsService internalUserDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        //determine whitelist endpoints
        for (String endpoint : ENDPOINTS_WHITELIST) {
            if (new AntPathRequestMatcher(endpoint).matches(request)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        final String authHeader = request.getHeader("Authorization");
        final String loanSubmissionBypassToken = request.getParameter("token"); //determine loan-submission whitelist when token integrate valid

        boolean invalidBouwheer = false;
        if (
                !StringUtil.isNullOrEmpty(loanSubmissionBypassToken)
                        //&& new AntPathRequestMatcher("/api/v1/loan-submissions").matches(request)
                        && new OrRequestMatcher(
                        new AntPathRequestMatcher("/api/v1/loan-submissions"),
                        new AntPathRequestMatcher("/api/v1/loan-submissions/invoices"),
                        new AntPathRequestMatcher("/api/v1/loan-submissions/importance-notes"),
                        new AntPathRequestMatcher("/api/v1/loan-submissions/simulations/percentage"),
                        new AntPathRequestMatcher("/api/v1/loan-submissions/simulations/calculate")
                ).matches(request)
        ) {
            if (
                    loanSubmissionBypassToken.equalsIgnoreCase("1")
                            || loanSubmissionBypassToken.equalsIgnoreCase("2")
                            || loanSubmissionBypassToken.equalsIgnoreCase("3")
            ) {
                filterChain.doFilter(request, response);
                return;
            }

            if (new AntPathRequestMatcher("/api/v1/loan-submissions/**").matches(request)) {
                JwtSimulasiModel jwtSimulasiModel = jwtLoanSubmissionService.extractToken(loanSubmissionBypassToken);
                if (jwtSimulasiModel == null) {
                    invalidBouwheer = true;
                } else if (!StringUtil.isNullOrEmpty(jwtSimulasiModel.getBouwheerCode())) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        } else {
            if (authHeader == null) {
                for (String endpoint : ENDPOINTS_WHITELIST_LOAN_SUBMISSION) {
                    if (new AntPathRequestMatcher(endpoint).matches(request)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            }
        }

        if (invalidBouwheer) {
            handlerExceptionResolver.resolveException(request, response, null, new Exception("External request need passing an valid token"));
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring("Bearer ".length());
            final String username = jwtService.extractUsername(jwt);
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && authentication == null) {
                UserDetails user;
                try {
                    user = userDetailsService.loadUserByUsername(username);
                } catch (Exception e) {
                    user = internalUserDetailsService.loadUserByUsername(username);
                }

                if (jwtService.isTokenValid(jwt, user)) {
                    authenticate(user, request);
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException | BadCredentialsException e) {
            request.setAttribute("exception", e);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("failed on set user authentication, {}", e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private void authenticate(UserDetails userDetails, @NonNull HttpServletRequest request) {
        final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authToken.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void notSecureRefreshToken() {
         /*String isRefreshToken = request.getHeader("isRefreshToken");
            String requestURL = request.getRequestURL().toString();

            if (isRefreshToken != null && isRefreshToken.equals("true") && requestURL.contains("refresh-token")) {
                allowForRefreshToken(e, request);
            } else {
                request.setAttribute("exception", e);
            }*/
    }

    private void allowForRefreshToken(ExpiredJwtException ex, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                null,
                null,
                null
        );
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        request.setAttribute("claims", ex.getClaims());
    }
}
