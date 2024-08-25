package com.kmkbe.modules.user.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.user.dto.UserDto;
import com.kmkbe.modules.user.service.UserInternalServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SignatureException;

@RestController
@RequestMapping("/api/v1/internal/user")
@Tag(
        name = "Internal User",
        description = "Internal User Endpoints"
)
@RequiredArgsConstructor
public class UserInternalController {
    private final UserInternalServices userInternalServices;

    @GetMapping
    public CommonResult<UserDto> getProfile(
            Authentication authentication
    ) throws SignatureException {
        return new CommonResult<UserDto>().success(
                userInternalServices.profile(authentication)
        );
    }

}
