package com.kmkbe.modules.internal.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.remote.dto.UserRemoteDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/user")
@Tag(
        name = "Internal User",
        description = "Internal User Endpoints"
)
@RequiredArgsConstructor
public class UserRemoteController {
    @PostMapping("/sign-in")
    public CommonResult<UserRemoteDto> signIn(
            @Valid @RequestBody LoginRequest request
    ) {
        return new CommonResult<UserRemoteDto>().success(null);
    }
}
