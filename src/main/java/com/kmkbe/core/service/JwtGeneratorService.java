package com.kmkbe.core.service;

import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * Service untuk membuat (generate) JWT Token
 * Kompatibel dengan JwtValidatorService
 *
 * Alur:
 * 1. Lookup app_secret dari DB berdasarkan ApiKey
 * 2. Build claims (payload)
 * 3. Sign dengan HMACSHA256 menggunakan app_secret
 * 4. Return JWT string: header.payload.signature
 */
@Service
public class JwtGeneratorService {

    @Autowired
    private ApiSbuRepository apiSbuRepository;

    /**
     * Generate JWT token
     * @param apiKey    - dari header "ApiKey", untuk lookup secret di DB
     * @param bouwheer  - identitas user/tenant (masuk ke payload claims)
     * @param expireInSeconds - durasi token valid (contoh: 3600 = 1 jam)
     * @return JWT string siap pakai
     */
    public String generateToken(String apiKey, String appSecret, String bouwheer, Date expireDate) {

        // ...lookup app_secret sama seperti sebelumnya...

        Date now  = new Date();


        String jwtToken = Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .claim("bouwheer_code", bouwheer)
                .setIssuedAt(now)
                .setExpiration(expireDate)   // ← expired 10 menit dari sekarang
                .signWith(
                        Keys.hmacShaKeyFor(appSecret.getBytes()),
                        SignatureAlgorithm.HS256
                )
                .compact();

        return jwtToken;
    }
    public String generateToken(String apiKey, String bouwheer, long expireInSeconds) {

        // === STEP 1: Lookup app_secret dari DB ===
        Optional<ApiSbu> appSbuOpt = apiSbuRepository.findByAppKey(apiKey);
        if (appSbuOpt.isEmpty()) {
            throw new IllegalArgumentException("ApiKey tidak ditemukan / tidak valid");
        }

        String appSecret = appSbuOpt.get().getAppSecret();

        // === STEP 2: Hitung waktu expired ===
        Date now        = new Date();
        Date expireDate = new Date(now.getTime() + (expireInSeconds * 1000));

        // === STEP 3: Build & sign JWT ===
        String jwtToken = Jwts.builder()
                .setHeaderParam("alg", "HS256")   // algorithm
                .setHeaderParam("typ", "JWT")      // type
                .claim("bouwheer_code", bouwheer)        // custom claim — sama seperti yg di-read validator
                .setIssuedAt(now)                     // iat: waktu dibuat
                .setExpiration(expireDate)          // exp: waktu expired — dicek di validator STEP 5
                .signWith(
                        Keys.hmacShaKeyFor(appSecret.getBytes()),  // secret dari DB
                        SignatureAlgorithm.HS256                   // harus sama dg validator
                )
                .compact();                        // hasilkan string header.payload.signature

        System.out.println("[JWT] Generated token untuk bouwheer: " + bouwheer);
        System.out.println("[JWT] Expired at: " + expireDate);

        return jwtToken;
    }
}
