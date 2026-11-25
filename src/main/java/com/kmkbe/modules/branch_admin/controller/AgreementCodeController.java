package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.SitDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.service.AgreementCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sit")
@RequiredArgsConstructor
public class AgreementCodeController {

    private final AgreementCodeService agreementCodeService;

    @GetMapping("/agreement/{financingHdrCode}")
    public CommonResult<SitDto> getAgreementsByFinancingHdrCode(@PathVariable("financingHdrCode") UUID financingHdrCode) {
        return agreementCodeService.getAgreementsByFinancingHdrCode(financingHdrCode);
    }
}
