package com.kmkbe.core.service;

import com.kmkbe.core.model.JwtSimulasiModel;
import com.kmkbe.core.utils.ObjectUtils;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

@Service
public class JwtLoanSubmissionService {
    @Value("${security.jwt.secret-key}")
    public String secretKey;

    public JwtSimulasiModel extractToken(String token) {
        if (StringUtil.isNullOrEmpty(token)) {
            return null;
        }

        byte[] payloadBytes = Base64.getUrlDecoder().decode(splitter(token, 0));
        String payloadStr = new String(payloadBytes, StandardCharsets.UTF_8);

        try {
            if (!validateSecret(token, payloadStr)) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        final Map<String, Object> payloadObj = ObjectUtils.strToJson(payloadStr);
        if (payloadObj != null) {
            return new JwtSimulasiModel() {
                @Override
                public String getBouwheerCode() {
                    return payloadObj.get("BouwheerCode").toString();
                }

                @Override
                public String getVendorCode() {
                    return payloadObj.get("VendorCode").toString();
                }

                @Override
                public String getSignature() {
                    return payloadObj.get("Signature").toString();
                }

                @Override
                public String getCreatedDateString() {
                    return payloadObj.get("CreatedDate").toString();
                }
            };
        }

        return null;
    }

    public boolean validateSecret(String token, String payloadStr) throws NoSuchAlgorithmException {
        final byte[] encodedPayloadWithSecret = Base64.getUrlEncoder().encode(
                (payloadStr + "." + secretKey).getBytes(StandardCharsets.UTF_8)
        );

        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(encodedPayloadWithSecret);
        byte[] digest = messageDigest.digest();
        String digestStr = Base64.getUrlEncoder().encodeToString(digest);

        return digestStr.equals(splitter(token, 1));
    }

    private String splitter(String token, int index) {
        String[] splitter = token.split("\\.");
        if (splitter.length <= index) {
            return null;
        }

        return splitter[index];
    }

}
