package com.kmkbe.core.service;

import com.kmkbe.core.model.JwtSimulasiModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtSimulasiService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;



    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;

    public JwtSimulasiService(@Lazy JwtDecoder jwtDecoder, @Lazy JwtEncoder jwtEncoder) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
    }



    public JwtSimulasiModel extractToken(String token  ) {
        final Claims claims = extractAllClaims(token);
        JwtSimulasiModel userDetails = new JwtSimulasiModel(){

            @Override
            public String getBouwheerCode() {
                return claims.get("BouwheerCode", String.class);
            }

            @Override
            public String getVendorCode() {
                return claims.get("VendorCode", String.class);
            }

            @Override
            public String getSignature() {
                return claims.get("Signature", String.class);
            }

            @Override
            public String getCreatedDateString() {
                return claims.get("CreatedDate", String.class);
            }
        };
        return userDetails;
    }


    public String generateToken(JwtSimulasiModel userDetails) {
        return Jwts.builder()
                .setClaims(fromSimulasiModel(userDetails))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private HashMap fromSimulasiModel(JwtSimulasiModel jwtSimulasiModel) {
        HashMap<String, Object> map = new HashMap<String, Object> ();
        map.put("CreatedDate", jwtSimulasiModel.getCreatedDateString());
        map.put("Signature", jwtSimulasiModel.getSignature());
        map.put("VendorCode", jwtSimulasiModel.getVendorCode());
        map.put("BouwheerCode", jwtSimulasiModel.getBouwheerCode());
        return map;
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(
            JwtSimulasiModel userDetails ) {
        return Jwts
                .builder()
                .setClaims(fromSimulasiModel(userDetails))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, JwtSimulasiModel userDetails) {
        final String username = extractUsername(token);
        return (username.equals("")) && !isTokenExpired(token);
    }

    public String generateOauth2Token(Authentication authentication) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
