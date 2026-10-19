package com.kmkbe.core.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    public String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value("${jwt.refreshExpirationDateInMs}")
    private int refreshExpirationDateInMs;

    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    public JwtService(@Lazy JwtDecoder jwtDecoder, @Lazy JwtEncoder jwtEncoder, Clock clock) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractToken(String token, Function<Claims, T> resolver) {
        return extractClaim(token, resolver);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpirationDateInMs);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public long getRefreshTokenExpirationTime() {
        return refreshExpirationDateInMs;
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        Instant now = clock.instant();

        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuer(userDetails.getUsername() + "@DanaSakti")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .audience().add("https://dev1-danasakti.csulfinance.com").and()
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public static final String DEFAULT_AUDIENCE = "https://dev1-danasakti.csulfinance.com";
    public static final String ISSUER_SUFFIX = "@DanaSakti";

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || userDetails == null) {
            return false;
        }
        final String username = extractUsername(token);
        if (username == null || !username.equals(userDetails.getUsername())) {
            return false;
        }
        if (isTokenExpired(token)) {
            return false;
        }

        // Validasi issuer & audience (Issue H1)
        Claims claims = extractAllClaims(token);
        String issuer = claims.getIssuer();
        if (issuer != null && !issuer.isBlank()) {
            if (!issuer.endsWith(ISSUER_SUFFIX) && !issuer.equals(username)) {
                return false;
            }
        }

        java.util.Set<String> audience = claims.getAudience();
        if (audience != null && !audience.isEmpty()) {
            boolean matchesAudience = audience.stream().anyMatch(aud ->
                    aud.contains("danasakti") || aud.contains("csulfinance") || aud.contains("localhost")
            );
            if (!matchesAudience) {
                return false;
            }
        }

        return true;
    }

    public String generateOauth2Token(Authentication authentication) {
        Instant now = clock.instant();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(clock.instant()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    protected Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .clock(() -> Date.from(clock.instant()))
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            // Fallback ke UTF-8 bytes jika secret bukan format base64
            keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT Secret Key must be at least 256 bits (32 bytes). Current length: " + (keyBytes.length * 8) + " bits");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
