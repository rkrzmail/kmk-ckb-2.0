package com.kmkbe.core.service;

import com.kmkbe.core.model.JwtSimulasiModel;
import com.kmkbe.core.utils.ObjectUtils;
import io.netty.util.internal.StringUtil;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class JwtLoanSubmissionService {
    @Value("${csul.loan-submission.secret-key}")
    public String secretKey;

    public JwtSimulasiModel extractToken(String token) {
        if (StringUtil.isNullOrEmpty(token)) {
            return null;
        }

        byte[] payloadBytes = Base64.getUrlDecoder().decode(splitter(token, 0));
        String payloadStr = new String(payloadBytes, StandardCharsets.UTF_8);

        try {
            if (!validateSecret(token)) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        final Map<String, Object> payloadObj = ObjectUtils.strToJson(payloadStr);
        if (payloadObj != null && payloadObj.get("BouwheerCode") != null && payloadObj.get("VendorCode") != null) {
            return new JwtSimulasiModel() {
                @Override
                public String getBouwheerCode() {
                    if (payloadObj.get("BouwheerCode") != null) {
                        return payloadObj.get("BouwheerCode").toString();
                    }

                    return null;
                }

                @Override
                public String getVendorCode() {
                    if (payloadObj.get("VendorCode") != null) {
                        return payloadObj.get("VendorCode").toString();
                    }

                    return null;
                }

                @Override
                public String getSignature() {
                    if (payloadObj.get("Signature") != null) {
                        return payloadObj.get("Signature").toString();
                    }
                    return null;
                }

                @Override
                public String getCreatedDateString() {
                    if (payloadObj.get("CreatedDate") != null) {
                        return payloadObj.get("CreatedDate").toString();
                    }

                    return null;
                }
            };
        }

        return null;
    }

    public boolean validateSecret(String token) throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update((splitter(token, 0) + "." + "DFA42k6CEhLSiX6o1zIPWUlzQGezQfHH").getBytes());
        byte[] digest = messageDigest.digest();

        String myHash = DatatypeConverter.printHexBinary(digest);
        return myHash.equalsIgnoreCase(splitter(token, 1));
    }

    private String splitter(String token, int index) {
        String[] splitter = token.split("\\.");
        if (splitter.length <= index) {
            return null;
        }

        return splitter[index].trim();
    }

}
