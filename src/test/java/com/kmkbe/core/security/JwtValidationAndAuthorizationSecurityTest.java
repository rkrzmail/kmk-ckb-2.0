package com.kmkbe.core.security;

import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtValidationAndAuthorizationSecurityTest {

    private static final String SECRET_KEY_HEX = "84b6e04f3268b78e7fee1883733e47aff50b88d1184b3512f2b5bffadb99d7eb";

    @Mock private JwtDecoder jwtDecoder;
    @Mock private JwtEncoder jwtEncoder;

    private JwtService jwtService;
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-19T10:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtDecoder, jwtEncoder, clock);
        jwtService.secretKey = SECRET_KEY_HEX;
    }

    @Test
    @DisplayName("H1: Valid token with correct issuer and audience passes validation")
    void validTokenPassesValidation() {
        Customer customer = new Customer();
        customer.setCustEmail("nasabah@example.com");

        String token = jwtService.generateToken(customer);

        assertThat(jwtService.isTokenValid(token, customer)).isTrue();
    }

    @Test
    @DisplayName("H1: Token with untrusted audience or forged issuer is rejected")
    void tokenWithForgedIssuerOrAudienceIsRejected() {
        Customer customer = new Customer();
        customer.setCustEmail("nasabah@example.com");

        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY_HEX);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        // 1. Untrusted audience
        String forgedAudienceToken = Jwts.builder()
                .subject("nasabah@example.com")
                .issuer("nasabah@example.com" + JwtService.ISSUER_SUFFIX)
                .audience().add("https://evil-phishing-site.com").and()
                .issuedAt(Date.from(clock.instant()))
                .expiration(Date.from(clock.instant().plusSeconds(3600)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThat(jwtService.isTokenValid(forgedAudienceToken, customer)).isFalse();

        // 2. Forged untrusted issuer
        String forgedIssuerToken = Jwts.builder()
                .subject("nasabah@example.com")
                .issuer("attacker@HackerDomain")
                .audience().add("https://dev1-danasakti.csulfinance.com").and()
                .issuedAt(Date.from(clock.instant()))
                .expiration(Date.from(clock.instant().plusSeconds(3600)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThat(jwtService.isTokenValid(forgedIssuerToken, customer)).isFalse();
    }

    @Test
    @DisplayName("H1: MstUser authorities include ROLE_USER and INTERNAL_USER for RBAC guard")
    void mstUserHasInternalAuthorities() {
        MstUser internalUser = MstUser.builder()
                .username("ao.user")
                .build();

        var authorities = internalUser.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        assertThat(authorities).contains("ROLE_USER", "INTERNAL_USER");
    }

    @Test
    @DisplayName("H1: Customer authorities are isolated with ROLE_CUSTOMER and not INTERNAL_USER")
    void customerHasCustomerAuthorityOnly() {
        Customer customer = new Customer();
        customer.setCustEmail("cust@example.com");

        var authorities = customer.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        assertThat(authorities).containsExactly("ROLE_CUSTOMER");
        assertThat(authorities).doesNotContain("INTERNAL_USER");
    }
}
