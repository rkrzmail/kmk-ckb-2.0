package com.kmkbe.modules.major_account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/branch-area")
@Tag(
        name = "Penempatan cabang API",
        description = "Berisi endpoints data penembatan cabang / branch area mapping"
)
@RequiredArgsConstructor
public class BranchAreaController {
}
