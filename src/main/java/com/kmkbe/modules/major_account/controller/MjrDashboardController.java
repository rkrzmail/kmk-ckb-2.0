package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.MjrAccDashboardDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.major_account.request.MjrDashboardRequest;
import com.kmkbe.modules.major_account.service.MjrDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SignatureException;

@Validated
@RestController
@RequestMapping("/api/v1/mjr/dashboard")
@Tag(
        name = "Major Account Dashboard API",
        description = "Berisi endpoints data dashboard Major Account"
)
@RequiredArgsConstructor
public class MjrDashboardController {
    private final MjrDashboardService mjrDashboardService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public CommonResult<MjrAccDashboardDto> getDashboard(
            MjrDashboardRequest request
    ) throws SignatureException {
        currentUserService.authenticatedInternalUser();
        return new CommonResult<MjrAccDashboardDto>().success(
                mjrDashboardService.calculate(request)
        );
    }
}
