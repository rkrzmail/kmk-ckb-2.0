package com.kmkbe.core.service;

import com.kmkbe.core.domain.model.JwtSimulasiModel;
import com.kmkbe.core.utils.ObjectUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtLoanSubmissionService {
    private final KeySecretServices keySecretServices;

    public JwtSimulasiModel extractToken(String token) {
        if (StringUtil.isNullOrEmpty(token)) {
            return null;
        }

        final String payloadStr = keySecretServices.decodePayloadAsString(token);

        try {
            if (!keySecretServices.validate(token)) {
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
}
