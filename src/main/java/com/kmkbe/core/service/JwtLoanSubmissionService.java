package com.kmkbe.core.service;

import com.kmkbe.core.model.JwtSimulasiModel;
import com.kmkbe.core.utils.ObjectUtils;
import io.netty.util.internal.StringUtil;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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

       /* try {
            if (!validateSecret(token, payloadStr)) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }*/

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

    public boolean validateSecret(String token, String payloadStr) throws NoSuchAlgorithmException {
        final String payloadManualEncoded = Base64.getUrlEncoder().encodeToString(payloadStr.getBytes()).trim();
        final String base64UrlEncodedPayload = splitter(token, 0);
        if (payloadManualEncoded.trim().equals(base64UrlEncodedPayload.trim())) {
            log.error("base64UrlEncodedPayload: {}", "same as manual encoded");
        }

        String mb0 = DigestUtils
                .md5DigestAsHex((base64UrlEncodedPayload + "." + "DFA42k6CEhLSiX6o1zIPWUlzQGezQfHH".toLowerCase()).getBytes());
        String mb00 = Base64.getUrlEncoder().encodeToString(mb0.getBytes());

        String mb01 = DigestUtils
                .md5DigestAsHex((base64UrlEncodedPayload + "." + "DFA42k6CEhLSiX6o1zIPWUlzQGezQfHH".toUpperCase()).getBytes());
        String mb001 = Base64.getUrlEncoder().encodeToString(mb01.getBytes());

        String mb1 = DigestUtils
                .md5DigestAsHex((payloadManualEncoded + "." + "DFA42k6CEhLSiX6o1zIPWUlzQGezQfHH".toLowerCase()).getBytes());
        String mb11 = Base64.getUrlEncoder().encodeToString(mb1.getBytes());

        String mb2 = DigestUtils
                .md5DigestAsHex((payloadManualEncoded + "." + "DFA42k6CEhLSiX6o1zIPWUlzQGezQfHH".toUpperCase()).getBytes());
        String mb22 = Base64.getUrlEncoder().encodeToString(mb2.getBytes());

        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update((payloadManualEncoded + "." + "DFA42k6CEhLSiX6o1zIPWUlzQGezQfHH").getBytes());
        byte[] digest = messageDigest.digest();

        //sample generated: NPyzGDA6R6mdwl47ksXeKRfrecGDjLcVoyIszAJDXrg
        String myHash = DatatypeConverter
                .printHexBinary(digest);
        String digestStr = Base64.getUrlEncoder().encodeToString(digest);
        String encrypted = splitter(token, 1);
        String e1 = new String(Base64.getUrlDecoder().decode(encrypted));

        return digestStr.equals(splitter(token, 1));
    }

    private String splitter(String token, int index) {
        String[] splitter = token.split("\\.");
        if (splitter.length <= index) {
            return null;
        }

        return splitter[index].trim();
    }

}
