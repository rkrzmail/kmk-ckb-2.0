package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.BranchDto;
import com.kmkbe.core.domain.dto.MjrAccDashboardDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.major_account.request.MjrDashboardRequest;
import com.kmkbe.modules.major_account.service.MjrDashboardService;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SignatureException;
import java.util.List;

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

    @GetMapping
    public CommonResult<MjrAccDashboardDto> getDashboard(
            Authentication authentication,
            MjrDashboardRequest request
    ) throws SignatureException {
        final MstUser mstUser = UserInternalUtils.authenticateUser(authentication);

        return new CommonResult<MjrAccDashboardDto>().success(
                mjrDashboardService.calculate(request)
        );
    }
}
