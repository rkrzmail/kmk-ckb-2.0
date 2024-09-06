package com.kmkbe.modules.common.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/general-setting")
@Tag(
        name = "General Setting",
        description = "Endpoint properti umum"
)
@RequiredArgsConstructor
public class GeneralSettingController {

}
