package com.kmkbe.modules.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.common.dto.LoginDto;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.user.request.LoginInternalRequest;
import com.kmkbe.modules.user.service.AuthInternalServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/auth")
@Tag(
        name = "Internal Authentication User",
        description = "Internal Authentication Endpoints"
)
@RequiredArgsConstructor
public class AuthInternalController {
    private final AuthInternalServices authInternalServices;

    @PostMapping("/sign-in")
    public CommonResult<LoginDto> signIn(
            @Valid @RequestBody LoginInternalRequest request
    ) throws JsonProcessingException {
        return new CommonResult<LoginDto>().success(
                authInternalServices.signIn(request)
        );
    }

    @PostMapping("/refresh-token")
    public CommonResult<LoginDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) throws Exception {
        return new CommonResult<LoginDto>().success(
                authInternalServices.refreshToken(request)
        );
    }
}
