package com.kmkbe.modules.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.user.dto.UserDto;
import com.kmkbe.modules.user.request.LoginInternalRequest;
import com.kmkbe.modules.user.services.UserInternalServices;
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
public class UserInternalController {
    private final UserInternalServices userInternalServices;

    @PostMapping("/sign-in")
    public CommonResult<UserDto> signIn(
            @Valid @RequestBody LoginInternalRequest request
    ) throws JsonProcessingException {
        return new CommonResult<UserDto>().success(
                userInternalServices.signIn(request)
        );
    }
}
