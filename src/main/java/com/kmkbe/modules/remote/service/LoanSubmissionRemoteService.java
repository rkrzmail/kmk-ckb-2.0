package com.kmkbe.modules.remote.service;

import com.kmkbe.core.service.BaseRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanSubmissionRemoteService {
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;

    // vendor same as bouwheer
    public Object fetchVendor() {
        return new Object();
    }

    public List<Object> fetchLegalDocument() {
        return new ArrayList<>();
    }

    /*private List<PostedInvoiceDto> testingDummy() {
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

    }*/
}
