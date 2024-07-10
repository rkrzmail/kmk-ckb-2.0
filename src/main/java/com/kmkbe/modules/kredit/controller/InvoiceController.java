package com.kmkbe.modules.kredit.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.kredit.request.CreateInvoiceRequest;
import com.kmkbe.modules.kredit.service.InvoiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
@Tag(
        name = "Invoice",
        description = "Berisi endpoints data Invoice dari proses integrasi API System MST"
)
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping("/create")
    public CommonResult<Object> create(@RequestBody CreateInvoiceRequest request) throws Exception {
        return new CommonResult<>().success(invoiceService.create(request));
    }
}
