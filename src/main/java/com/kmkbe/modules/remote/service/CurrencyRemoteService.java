package com.kmkbe.modules.remote.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyRemoteService {
    private final RestTemplate restTemplate;

    public double fetchIdrFrom(String baseCurrency) {
        try {
            ResponseEntity<Map<String, Object>> response;
            try {
                response = restTemplate.exchange(
                        "https://latest.currency-api.pages.dev/v1/currencies/" + baseCurrency.toLowerCase() + ".min.json",
                        HttpMethod.GET,
                        new HttpEntity<>(
                                null,
                                null
                        ),
                        new ParameterizedTypeReference<>() {
                        }
                );
            } catch (Exception e) {
                response = restTemplate.exchange(
                        "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/" + baseCurrency.toLowerCase() + ".min.json",
                        HttpMethod.GET,
                        new HttpEntity<>(
                                null,
                                null
                        ),
                        new ParameterizedTypeReference<>() {
                        }
                );
            }

            if (
                    response.getBody() != null
                            && response.getBody().get(baseCurrency.toLowerCase()) != null
            ) {
                if (((Map<?, ?>) response.getBody().get(baseCurrency.toLowerCase())).get("idr") != null) {
                    BigDecimal result = BigDecimal.valueOf(
                                    Double.parseDouble(
                                            ((Map<?, ?>) response.getBody().get(baseCurrency.toLowerCase()))
                                                    .get("idr")
                                                    .toString()
                                    )
                            )
                            .setScale(2, RoundingMode.CEILING);

                    return result.doubleValue();
                }
            }

            return 15000.00;
        } catch (Exception e) {
            log.error("fetchIdr: error {}", e.getMessage());
            throw e;
        }
    }
}
