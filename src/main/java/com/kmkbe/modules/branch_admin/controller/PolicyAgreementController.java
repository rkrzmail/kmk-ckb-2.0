package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.PolicyAgreementDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.service.PolicyAgreementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policy-agreements")
public class PolicyAgreementController {

    @Autowired
    private PolicyAgreementService policyAgreementService;

    @PostMapping("/create")
    public CommonResult<PolicyAgreementDto> createPolicyAgreement(@RequestBody PolicyAgreementDto policyAgreementDto) {
        return policyAgreementService.createPolicyAgreement(policyAgreementDto);
    }


    @GetMapping("/list")
    public CommonResult<List<PolicyAgreementDto>> getPolicyAgreementList() {
        List<PolicyAgreementDto> policyAgreementList = policyAgreementService.getPolicyAgreementList();
        return new CommonResult<List<PolicyAgreementDto>>().success(policyAgreementList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResult<PolicyAgreementDto>> getPolicyAgreementById(@PathVariable Long id) {
        CommonResult<PolicyAgreementDto> result = policyAgreementService.getPolicyAgreementById(id);

        if (result.getIsSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<CommonResult<PolicyAgreementDto>> updatePolicyAgreement(
            @PathVariable Long id,
            @RequestBody PolicyAgreementDto policyAgreementDto
    ) {
        CommonResult<PolicyAgreementDto> result = policyAgreementService.updatePolicyAgreement(id, policyAgreementDto);

        if (result.getIsSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }

}
