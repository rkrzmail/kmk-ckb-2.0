package com.kmkbe.modules.kredit.service;

import com.kmkbe.modules.kredit.repository.InvoiceRepository;
import com.kmkbe.modules.kredit.request.CreateInvoiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public String create(CreateInvoiceRequest request) throws Exception {
        try {
            throw new Exception("Api not yet implemented");
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }
}
