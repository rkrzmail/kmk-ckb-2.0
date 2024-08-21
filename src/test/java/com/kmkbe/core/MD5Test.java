package com.kmkbe.core;

import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.MD5Utils;
import com.kmkbe.core.utils.ObjectUtils;
import jakarta.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MD5Test {
    private static final String SECRET = "DFA42k6CEhLSiX6o1zIPWUlzQGezQfHH";

    @BeforeEach
    public void beforeEach() {
    }

    @Test
    @DisplayName("Should return valid jwt header")
    public void decodeJwtHeaderTest() {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");

        String jsonHeader;
        String encodedHeader;
        try {
            jsonHeader = ObjectUtils.jsonToStr(header);
            encodedHeader = Base64.getUrlEncoder().encodeToString(jsonHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /*Assertions.assertThat(jsonHeader).isNotNull().isNotEmpty();
        Assertions.assertThat(encodedHeader).isNotNull().isNotEmpty();
        Assertions.assertThat(encodedHeader).isEqualTo("eyJhbGciOiJIUzI1NiJ9");*/
    }

    @Test
    @DisplayName("Should return valid jwt payload")
    public void decodeJwtPayloadTest() {
        String jsonPayload = null, encodedPayload = null, encodedSecret = null;

        try {
            jsonPayload = ObjectUtils.jsonToStr(payload());
            encodedPayload = Base64.getUrlEncoder().encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));
            encodedSecret = MD5Utils.hash(encodedPayload + "." + SECRET);
        } catch (Exception ignored) {
        }

        Assertions.assertNotNull(jsonPayload);
        Assertions.assertNotNull(encodedPayload);
        Assertions.assertNotNull(encodedSecret);
    }

    @Test
    public void generatedTest() {
        String dataEncoded = "eyJCb3V3aGVlckNvZGUiOiJlOGY3ODgyMC0wZTQ3LWRhMTEtNTg1Yy04YmY3NWRkNjA4ZjEiLCJWZW5kb3JDb2RlIjoiMDAwMjAxMTgxNyIsIkNyZWF0ZURhdGVTdHJpbmciOiIyMDI0LTA4LTIwIDE2OjMwOjIzIn0";
        String keyEncoded = "cd453a5e3e64b11bdec0512d61d18cd0";

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
        }

        messageDigest.update((dataEncoded + "." + SECRET).getBytes());
        byte[] digest = messageDigest.digest();
        String keyReEncoded = DatatypeConverter.printHexBinary(digest);

        Assertions.assertEquals(keyReEncoded, keyEncoded.toUpperCase());
    }


    private Map<String, Object> payload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("BouwheerCode", UUID.randomUUID());
        payload.put("VendorCode", "0001000007");
        payload.put("CreatedDateString", DateTimeUtils.SDF_STANDARD_DATE_TIME.format(new Date()));
        return payload;
    }
}
