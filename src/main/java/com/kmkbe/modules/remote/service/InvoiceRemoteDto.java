package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.core.domain.dto.InquiryInvoiceRemoteDto;
import com.kmkbe.modules.remote.request.InquiryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceRemoteDto {
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;

    public BaseSimpleRemoteResponseDto<InquiryInvoiceRemoteDto> inquiryInvoice(
            String vendorCode
    ) throws JsonProcessingException {
        try {
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    ObjectUtils.jsonToStr(InquiryRequest.builder()
                            .vendorCode(vendorCode)
                            .build()),
                    baseRemoteService.apiKeyHeaders()
            );

            final ResponseEntity<BaseSimpleRemoteResponseDto<InquiryInvoiceRemoteDto>> response = restTemplate.exchange(
                    BaseRemoteService.BASE_URL_MST + "/sap/listPostedInvoiceByVendorInSAP",
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("inquiryInvoice, error {}", e.getMessage());
            throw e;
        }
    }
}
