package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
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

    public BaseSimpleRemoteResponseDto<CsulInquiryInvoiceRemoteDto> inquiryInvoice(
            String vendorCode
    ) throws JsonProcessingException {
        try {
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    ObjectUtils.jsonToStr(InquiryRequest.builder()
                            .vendorCode(vendorCode)
                            .build()),
                    baseRemoteService.apiKeyHeaders()
            );

            final ResponseEntity<BaseSimpleRemoteResponseDto<CsulInquiryInvoiceRemoteDto>> response = restTemplate.exchange(
                    baseRemoteService.getBaseMst() + "/sap/listPostedInvoiceByVendorInSAP",
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();


           /* final ResponseEntity<String> res = restTemplate.exchange(
                    baseRemoteService.getBaseMst() + "/sap/listPostedInvoiceByVendorInSAP",
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            int  o = res.getStatusCode().value();
            String stsr = String.valueOf(res.getBody());
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            // Daftarkan serializer global untuk tipe Double
  *//*          SimpleModule module = new SimpleModule();
            SimpleModule simpleModule = module.addSerializer(InquiryInvoiceRemoteDto.class, new InquiryInvoiceRemoteDto());
            om.registerModule(module);*//*

            BaseSimpleRemoteResponseDto<InquiryInvoiceRemoteDto> root= om.readValue(stsr, BaseSimpleRemoteResponseDto.class);
            InquiryInvoiceRemoteDto invoiceRemoteDto = root.getData();

            return  root; */
        } catch (Exception e) {


            log.error("inquiryInvoice, error {}", e.getMessage());
            throw e;
        }
    }
}
