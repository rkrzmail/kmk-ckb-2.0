package com.kmkbe.core;

import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MD5Test {

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

        Assertions.assertThat(jsonHeader).isNotNull().isNotEmpty();
        Assertions.assertThat(encodedHeader).isNotNull().isNotEmpty();
        Assertions.assertThat(encodedHeader).isEqualTo("eyJhbGciOiJIUzI1NiJ9");
    }

    @Test
    @DisplayName("Should return valid jwt payload")
    public void decodeJwtPayloadTest() {
        String jsonPayload;
        String encodedPayload;
        try {
            jsonPayload = ObjectUtils.jsonToStr(payload());
            encodedPayload = Base64.getUrlEncoder().encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        Assertions.assertThat(jsonPayload).isNotNull().isNotEmpty();
        Assertions.assertThat(encodedPayload).isNotNull().isNotEmpty();
    }


    private Map<String, Object> payload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("BouwheerCode", "1111");
        payload.put("Signature", "MST");
        payload.put("VendorCode", "1111");
        payload.put("CreatedDateString", DateTimeUtils.SDF_STANDARD_DATE_TIME.format(new Date()));
        return payload;
    }
}
