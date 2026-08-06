package com.kmkbe.core.service;

import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/**
 * Service untuk validasi JWT Token
 * <p>
 * Alur validasi:
 * 1. Ambil app_secret dari DB berdasarkan ApiKey (app_key)
 * 2. Pisah JWT menjadi: header.payload.signature
 * 3. Decode header & payload dari Base64Url
 * 4. Verifikasi signature: HMACSHA256(base64(header) + "." + base64(payload), app_secret)
 * 5. Cek exp (expiry time)
 */
@Slf4j
@Service
public class JwtValidatorService {

  @Autowired
  private ApiSbuRepository apiSbuRepository;

  /**
   * Validasi JWT token
   *
   * @param apiKey   - dari Request Header "ApiKey", dipakai untuk lookup secret di DB
   * @param jwtToken - token dari path URL
   * @return ValidationResponse berisi claims jika valid
   */

  public ValidationResponse validate(String apiKey, String jwtToken) {
    Optional<ApiSbu> appSbuOpt = apiSbuRepository.findByAppKey(apiKey);
    if (appSbuOpt.isEmpty()) {
      throw new IllegalArgumentException("ApiKey tidak ditemukan / tidak valid");
    }

    if(!appSbuOpt.get().getSesStatus().equals("ACTIVE")){
      log.info(ErrorConstant.ERROR_MESSAGE_83 + "{}", appSbuOpt.get().getSesStatus());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_83, ErrorConstant.ERROR_MESSAGE_83 + appSbuOpt.get().getSesStatus());
    }

    return validate(apiKey, jwtToken, appSbuOpt.get());
  }

  public ValidationResponse validate(String apiKey, String jwtToken, ApiSbu apiSbu) {

    // === STEP 1: Lookup app_secret dari DB berdasarkan app_key ===


    String appSecret = apiSbu.getAppSecret();

    // === STEP 2: Split token → header.payload.signature ===
    String[] parts = jwtToken.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Format JWT tidak valid, harus: header.payload.signature");
    }

    String base64Header = parts[0];
    String base64Payload = parts[1];
    String base64Signature = parts[2];

    // === STEP 3: Decode header & payload untuk logging/inspect ===
    String headerJson = decodeBase64Url(base64Header);
    String payloadJson = decodeBase64Url(base64Payload);

    System.out.println("[JWT] Header  : " + headerJson);
    System.out.println("[JWT] Payload : " + payloadJson);

    // === STEP 4: Verifikasi signature menggunakan JJWT library ===
    // JJWT akan otomatis: rebuild signature → bandingkan → throw jika beda
    Claims claims;
    try {
      claims = Jwts.parserBuilder()
        .setSigningKey(appSecret.getBytes())   // secret key dari DB
        .build()
        .parseClaimsJws(jwtToken)              // jika signature salah → throw SignatureException
        .getBody();
    } catch (ExpiredJwtException e) {
      throw e;   // re-throw, akan ditangkap di controller
    } catch (SignatureException e) {
      throw e;   // re-throw
    }

    // === STEP 5: Cek expiry ===
    Date expDate = claims.getExpiration();
    if (expDate != null && expDate.before(new Date())) {
      throw new ExpiredJwtException(null, claims, "Token sudah expired pada: " + expDate);
    }

    // === VALID ===
    ValidationResponse response = new ValidationResponse();
    response.setBouwheer(claims.get("bouwheer_code", String.class));
    response.setExp(expDate != null ? expDate.getTime() / 1000 : null);
    response.setRawHeader(headerJson);
    response.setRawPayload(payloadJson);
    response.setApiKey(apiKey);

    return response;
  }

  /**
   * Decode Base64Url → JSON string
   */
  private String decodeBase64Url(String base64UrlEncoded) {
    try {
      // Base64Url: ganti '-' → '+', '_' → '/', tambah padding '='
      String normalized = base64UrlEncoded
        .replace('-', '+')
        .replace('_', '/');

      // Tambah padding jika perlu
      int mod = normalized.length() % 4;
      if (mod == 2) normalized += "==";
      else if (mod == 3) normalized += "=";

      byte[] decoded = Base64.getDecoder().decode(normalized);
      return new String(decoded, "UTF-8");
    } catch (Exception e) {
      return "Could not decode: " + e.getMessage();
    }
  }
}
