package com.kmkbe.modules.kredit.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.kredit.request.CreateFinancingRequest;
import com.kmkbe.modules.kredit.service.FinancingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/financings")
@Tag(
        name = "Financing",
        description = "Berisi endpoints hasil simulasi dari pengajuan Kredit Debitur"
)
@RequiredArgsConstructor
public class FinancingController {
    private final FinancingService financingService;

    @PostMapping("/create")
    public CommonResult<Object> create(@RequestBody CreateFinancingRequest request) {
        return new CommonResult<>().success(new HashMap<>(), financingService.create(request));
    }
}
