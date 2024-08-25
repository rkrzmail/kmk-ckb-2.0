package com.kmkbe.modules.branch_admin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/cwr")
@Tag(
        name = "Persetujuan Kredit Endpoints",
        description = "Berisi endpoints data persetujuan/kelayakan kredit debitur"
)
@RequiredArgsConstructor
public class CwrController {
}
