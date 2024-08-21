package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.loan_submission.dto.BouwheerDto;
import com.kmkbe.modules.loan_submission.mapper.BouwheerMapper;
import com.kmkbe.modules.loan_submission.repository.BouwheerRepository;
import com.kmkbe.modules.remote.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.modules.remote.dto.InquiryInvoiceRemoteDto;
import com.kmkbe.modules.remote.dto.InquiryVendorRemoteDto;
import com.kmkbe.modules.remote.dto.PostedInvoiceDto;
import com.kmkbe.modules.remote.request.InquiryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanSubmissionRemoteService {
    private final BouwheerRepository bouwheerRepository;
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;

    // vendor same as bouwheer
    public Object fetchVendor() {
        return new Object();
    }

    public List<Object> fetchLegalDocument() {
        return new ArrayList<>();
    }

    public BaseSimpleRemoteResponseDto<InquiryVendorRemoteDto> inquiryVendor(
            String vendorCode
    ) throws JsonProcessingException {
        try {
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    ObjectUtils.jsonToStr(InquiryRequest.builder()
                            .vendorCode(vendorCode)
                            .build()),
                    baseRemoteService.apiKeyHeaders()
            );

            final ResponseEntity<BaseSimpleRemoteResponseDto<InquiryVendorRemoteDto>> response = restTemplate.exchange(
                    "https://6mn45m67ybarmii47vg4cc22240ltnmj.lambda-url.ap-southeast-1.on.aws/v1/vendor/byVendorId",
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("inquiryVendor, error {}", e.getMessage());
            throw e;
        }
    }

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
                    "https://6mn45m67ybarmii47vg4cc22240ltnmj.lambda-url.ap-southeast-1.on.aws/v1/sap/listPostedInvoiceByVendorInSAP",
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

    private List<PostedInvoiceDto> testingDummy() {
        final BouwheerDto bouwheer = BouwheerMapper.INSTANCE.fromEntity(
                bouwheerRepository.findByBouwheerCode(UUID.fromString("e8f78820-0e47-da11-585c-8bf75dd608f1")).get()
        );

        return Arrays.asList(
                PostedInvoiceDto.builder()
                        .bouwheerCode(bouwheer.getBouwheerCode().toString())
                        .bouwheerName(bouwheer.getBouwheerName())
                        .currencyCode("IDR")
                        .customerInvoiceNo("INV-001")
                        .bouwheerInvoiceNo("INV-B001")
                        .invoiceDescription("10 Mineral Water Aqua Galon")
                        .invoiceDate(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)))
                        .invoiceDueDate(Date.from(Instant.now().plus(11, ChronoUnit.DAYS)))
                        .invoiceAmount(BigDecimal.valueOf(10_000_000.0).setScale(2, RoundingMode.CEILING))
                        .build(),
                PostedInvoiceDto.builder()
                        .bouwheerCode(bouwheer.getBouwheerCode().toString())
                        .bouwheerName(bouwheer.getBouwheerName())
                        .currencyCode("IDR")
                        .customerInvoiceNo("INV-002")
                        .bouwheerInvoiceNo("INV-B002")
                        .invoiceDescription("15 Mineral Water Aqua Galon")
                        .invoiceDate(Date.from(Instant.now().plus(9, ChronoUnit.DAYS)))
                        .invoiceDueDate(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)))
                        .invoiceAmount(BigDecimal.valueOf(15_000_000.0).setScale(2, RoundingMode.CEILING))
                        .build(),
                PostedInvoiceDto.builder()
                        .bouwheerCode(bouwheer.getBouwheerCode().toString())
                        .bouwheerName(bouwheer.getBouwheerName())
                        .currencyCode("IDR")
                        .customerInvoiceNo("INV-003")
                        .bouwheerInvoiceNo("INV-B003")
                        .invoiceDescription("20 Mineral Water Aqua Galon")
                        .invoiceDate(Date.from(Instant.now().plus(8, ChronoUnit.DAYS)))
                        .invoiceDueDate(Date.from(Instant.now().plus(9, ChronoUnit.DAYS)))
                        .invoiceAmount(BigDecimal.valueOf(20_000_000.0).setScale(2, RoundingMode.CEILING))
                        .build()
        );

    }
}
