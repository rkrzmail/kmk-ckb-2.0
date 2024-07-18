package com.kmkbe.modules.external.service;

import com.kmkbe.modules.external.dto.PostedInvoiceDto;
import com.kmkbe.modules.loan_submission.dto.BouwheerDto;
import com.kmkbe.modules.loan_submission.mapper.BouwheerMapper;
import com.kmkbe.modules.loan_submission.repository.BouwheerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MSTLoanService {
    private final BouwheerRepository bouwheerRepository;
    private final RestTemplate restTemplate;

    // vendor same as bouwheer
    public Object fetchVendor() {
        return new Object();
    }

    public List<Object> fetchLegalDocument() {
        return new ArrayList<>();
    }

    public List<PostedInvoiceDto> fetchListOfPostedInvoice(Authentication authentication) {
        try {
            return testingDummy();
        } catch (Exception e) {
            log.error("Error while fetching list of posted invoice", e);
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
